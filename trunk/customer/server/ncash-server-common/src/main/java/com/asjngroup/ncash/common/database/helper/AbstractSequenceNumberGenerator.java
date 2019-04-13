package com.asjngroup.ncash.common.database.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;

public abstract class AbstractSequenceNumberGenerator implements SequenceGenerator
{

	protected static final Log log = LogFactory.getLog( IdGenerator.class );
	protected static final String ID_TABLE = "sequence_num_generator";

	protected static Map<String, Long> allocationSizes = new HashMap<String, Long>();
	protected static Map<String, IdData> allocatedIds = new HashMap<String, IdData>();

	class IdData
	{
		long currentId;
		long hiId;
	}

	public synchronized void setAllocationSize( String objectKey, long allocSize )
	{
		if ( allocSize == 0 || allocSize == 1 )
			return;

		allocationSizes.put( objectKey, allocSize );

		IdData idData = new IdData();
		idData.currentId = 0;
		idData.hiId = -1;
		allocatedIds.put( objectKey, idData );
	}

	public synchronized Integer generate( String objectKey, int required ) throws HibernateException
	{
		if ( required <= 0 )
		{
			throw new IllegalArgumentException( "Invalid required id size " + required );
		}

		return new Integer( generateLong( objectKey, ( long ) required ).intValue() );
	}

	public synchronized Integer generate( String objectKey ) throws HibernateException
	{
		return new Integer( generateLong( objectKey ).intValue() );
	}

	public synchronized Long generateLong( String objectKey, long required ) throws HibernateException
	{
		if ( required <= 0 )
		{
			throw new IllegalArgumentException( "Invalid required id size " + required );
		}

		return doGenerate( objectKey, required );
	}

	public synchronized Long generateLong( String objectKey ) throws HibernateException
	{
		if ( allocationSizes.containsKey( objectKey ) )
		{
			IdData idData = allocatedIds.get( objectKey );

			if ( idData.currentId > idData.hiId )
			{
				long allocationSize = allocationSizes.get( objectKey );
				long newLowId = doGenerateFromDataBase( objectKey, allocationSize );
				idData.hiId = newLowId + allocationSize - 1;
				idData.currentId = newLowId;
			}

			// allocate a number off the cache
			long ret = idData.currentId;
			idData.currentId++;
			return ret;
		}
		else
		{
			return doGenerateFromDataBase( objectKey, 1 );
		}
	}

	public synchronized Long doGenerate( String objectKey, long required ) throws HibernateException
	{
		// if this has a special allocation size then use any cached keys
		if ( allocationSizes.containsKey( objectKey ) )
		{
			IdData idData = allocatedIds.get( objectKey );

			// cache is empty, refill it
			if ( idData.currentId > idData.hiId )
			{
				long allocationSize = allocationSizes.get( objectKey );
				long newLowId = doGenerateFromDataBase( objectKey, allocationSize );
				idData.hiId = newLowId + allocationSize - 1;
				idData.currentId = newLowId;
			}

			// allocate a number off the cache
			long ret = idData.currentId;
			idData.currentId++;
			return ret;
		}
		else
		{
			// standard on-demand strategy
			return doGenerateFromDataBase( objectKey, required );
		}
	}

	protected Long doGenerateFromDataBase( String objectKey, long required ) throws HibernateException
	{
		long bufferForManualInsert = 100000;
		String query = getSelectQuery( objectKey, required );
		String update = getUpdateQuery( objectKey, required );
		String insert = getInsertQuery( objectKey, required + bufferForManualInsert );

		Session session = HibernateSession.openSessionWithTransaction();
		Connection conn = session.doReturningWork( new ReturningWork<Connection>()
		{
			public Connection execute( Connection conn ) throws SQLException
			{
				return conn;
			}
		} );
		boolean oldAutoCommit;
		long result;

		try
		{
			oldAutoCommit = conn.getAutoCommit();
		}
		catch ( SQLException e )
		{
			log.error( "Failed to store the autocommit mode of the connection", e );
			throw new HibernateException( e );
		}

		try
		{
			PreparedStatement ups = conn.prepareStatement( update );
			try
			{
				ups.executeUpdate();
			}
			catch ( SQLException sqle )
			{
				log.error( "Failed to update the hi value for object key: " + objectKey, sqle );
				throw sqle;
			}
			finally
			{
				if ( ups != null )
					ups.close();
			}

			PreparedStatement qps = conn.prepareStatement( query );
			ResultSet rs = null;
			try
			{
				rs = qps.executeQuery();
				if ( !rs.next() )
				{
					Statement insertStatement = null;
					try
					{
						insertStatement = conn.createStatement();
						int rowsInserted = insertStatement.executeUpdate( insert );

						if ( rowsInserted != 1 )
						{
							throw new SQLException( "Failed to insert a new hi value for object key: " + objectKey );
						}

						result = bufferForManualInsert + 1;
					}
					finally
					{
						if ( insertStatement != null )
						{
							try
							{
								insertStatement.close();
							}
							catch ( SQLException e )
							{
								log.error( "Failed to close insert statement", e );
							}
						}
					}
				}
				else
				{
					result = rs.getLong( 1 ) - required + 1;
				}
			}
			catch ( SQLException sqle )
			{
				log.error( "Failed to read a hi value for object key:" + objectKey, sqle );
				throw sqle;
			}
			finally
			{
				if ( rs != null )
				{
					try
					{
						rs.close();
					}
					catch ( SQLException e )
					{
						log.error( "Error closing result set", e );
					}
				}

				if ( qps != null )
				{
					try
					{
						qps.close();
					}
					catch ( SQLException e )
					{
						log.error( "Error closing prepared statement", e );
					}
				}
			}

			conn.commit();

			return new Long( result );
		}
		catch ( SQLException e )
		{
			try
			{
				conn.rollback();
			}
			catch ( SQLException e1 )
			{
				log.error( "Failed to rollback the transaction", e1 );
				throw new HibernateException( e );
			}
			throw new HibernateException( e );
		}
		finally
		{
			try
			{
				conn.setAutoCommit( oldAutoCommit );
			}
			catch ( SQLException e )
			{
				log.error( "Failed to reset the autocommit mode", e );
				throw new HibernateException( e );
			}

			session.close();
		}
	}

	public Long getBlock( String objectKey, long allocSize ) throws HibernateException
	{
		return doGenerateFromDataBase( objectKey, allocSize );
	}

	protected abstract String getSelectQuery( String objectName, long required );

	protected abstract String getUpdateQuery( String objectName, long required );

	protected abstract String getInsertQuery( String objectName, long required );

}
