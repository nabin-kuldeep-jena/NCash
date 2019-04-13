package com.asjngroup.ncash.common.database.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteOrder;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.asjngroup.ncash.common.NCashDateFormatter;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.schema.Column;
import com.asjngroup.ncash.common.database.schema.ColumnDataType;
import com.asjngroup.ncash.common.database.schema.Database;
import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.database.schema.SchemaHelper;
import com.asjngroup.ncash.common.database.schema.Table;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;
import com.asjngroup.ncash.common.xml.XMLHelper;

public abstract class AbstractDataSource implements DataSource
{
	public String getUserName()
	{
		return userName;
	}

	private static final Log log = LogFactory.getLog( AbstractDataSource.class );

	protected javax.sql.DataSource datasource;
	protected boolean isUnicode;

	protected Schema schema;

	private String userName;

	protected final static String DOT = ".";

	protected static final String[] INTERNAL_TBLS = new String[]
	{ "changed_class", "next_object_no" };

	private static final String TABLE_FOR_LOCK = "table_for_lock";
	private static final String TABLE_FOR_LOCK_COLUMN = "tfl_no";
	private static final String TABLE_FOR_LOCK_NAME = "tfl_name";

	public static final String OUTPUT_DELIMITER = "^^";

	private String database;

	protected AbstractDataSource( javax.sql.DataSource dataSource ) throws SQLException
	{
		this.datasource = dataSource;
		DatabaseMetaData dmd = datasource.getConnection().getMetaData();
		String url = dmd.getURL();
		this.database = url.substring( url.lastIndexOf( "/" ) + 1 );
		this.userName = dmd.getUserName().split( "@" )[0];
	}

	public boolean doesItExist( String sqlQuery ) throws DataSourceException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = getConnection();

			stmt = conn.createStatement();
			rs = stmt.executeQuery( sqlQuery );
			return rs.next();

		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}
		finally
		{
			if ( rs != null )
			{
				try
				{
					rs.close();
				}
				catch ( Exception e )
				{
					throw new DataSourceException( e );
				}
			}

			if ( stmt != null )
			{

				try
				{
					stmt.close();
				}
				catch ( SQLException e )
				{
					throw new DataSourceException( e );
				}
			}
			if ( conn != null )
			{
				// try
				// {
				// conn.close();
				// }
				// catch ( Exception e )
				// {
				// throw new DataSourceException(e);
				// }
			}
		}
	}

	protected String getProperty( Map<String, String> properties, String property )
	{
		String value = properties.get( property );
		if ( value == null )
			throw new NCashRuntimeException( property + " property is not specified" );
		return value;
	}

	protected String silentGetProperty( Properties properties, String property, String defaultValue )
	{
		String value = properties.getProperty( property );
		return ( value == null ? defaultValue : value );
	}

	public Connection getConnection() throws DataSourceException
	{
		try
		{
			Connection connection = datasource.getConnection();
			connection.setAutoCommit( false );
			return connection;
		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}
	}

	public void close( Connection connection )
	{
		// close connection pool
		try
		{
			connection.close();
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		}
	}

	public String getSqlDateConstant( DateTime dateTime )
	{
		return getSqlDateConstant( dateTime, getBcpDateTimeFormatter() );
	}

	public void attachSchema( Schema schema )
	{
		this.schema = schema;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public String getDatabase()
	{
		return database;
	}

	public javax.sql.DataSource getDataSource()
	{
		return datasource;
	}

	public Schema getSchema( String database ) throws DataSourceException
	{
		return getSchema( database, null );
	}

	public Schema getSchema( String database, String table ) throws DataSourceException
	{
		return getSchema( database, table, false );
	}

	public Schema getSchema( String database, String table, boolean syncWithNonncash ) throws DataSourceException
	{
		dopAllTempTables();
		// create the schema and database
		Schema dbSchema = new Schema();
		Database dbDatabase = new Database();
		dbDatabase.DatabaseName = database;

		dbSchema.Databases.add( dbDatabase );

		List<Object[]> result = executeQuery( getAllTableDetailsQuery( database, table ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Int, ColumnDataType.Bool, ColumnDataType.Int, ColumnDataType.Int } );

		Table newTable = null;
		boolean invalidTable = false;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];
			String columnName = ( String ) row[1];
			String dataType = ( String ) row[2];
			Integer length = ( Integer ) row[3];
			Boolean isNullable = ( Boolean ) row[4];
			Integer sequenceNumber = ( Integer ) row[5];
			Integer scale = ( Integer ) row[6];

			// table changed name so make a new one
			if ( newTable == null || !newTable.TableName.equals( tableName ) )
			{
				if ( newTable != null )
				{
					if ( invalidTable )
					{
						log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
					}
					else
					{
						dbDatabase.Tables.add( newTable );
					}
				}

				invalidTable = false;
				newTable = new Table();
				newTable.TableName = tableName;
				newTable.database = dbDatabase;

			}

			// create and populate a column
			Column column = new Column();
			column.ColumnName = columnName;

			// if length row is null (i.e typically on a text/clob data-type, dont perform unkownToInteger on it
			// (which cannot accept nulls) -- just set it to -1
			if ( length != null )
				column.Length = length;
			else
				column.Length = -1;

			column.DataType = dbTypeToColumnDataType( dataType, column.Length, scale, syncWithNonncash );

			// if we get a unknown column then ignore this table
			if ( column.DataType == null )
				invalidTable = true;

			// Text type is unlimited in length, so returns 'null' - avoid calling unknownToInteger on it
			column.IsMandatory = !isNullable;
			column.SequenceNumber = sequenceNumber;
			column.table = newTable;

			newTable.Columns.add( column );

			if ( newTable != null )
			{
				if ( invalidTable )
				{
					log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
				}
				else
				{
					dbDatabase.Tables.add( newTable );
				}
			}
		}

		// get the list of indexes
		result = executeQuery( getAllIndexDetailsQuery( database, table ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Int } );

		Index index = null;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];
			String indexName = ( String ) row[1];
			String columnName = ( String ) row[2];
			Boolean isClustered = ( Boolean ) row[3];
			Boolean isUnique = ( Boolean ) row[4];
			Boolean isIgnoreDupKey = ( Boolean ) row[5];

			// find the table for this index
			Table currentTable = dbDatabase.findTable( tableName );

			// skip if the table was not matched, may have been ignored above
			if ( currentTable == null )
				continue;

			// index/table changed name so make a new one
			if ( index == null || !index.IndexName.equals( indexName ) || !index.table.TableName.equals( currentTable.TableName ) )
			{
				index = new Index();
				index.IndexName = indexName;
				index.IsClustered = isClustered;
				index.IsUnique = isUnique;
				index.IsIgnoreDupKey = isIgnoreDupKey;

				index.table = currentTable;
				currentTable.Indexes.add( index );
			}

			// add the column name to the index
			index.addColumn( columnName );
		}

		return dbSchema;
	}

	private void dopAllTempTables() throws DataSourceException
	{
		String tempTableQuery = "SELECT TABLE_NAME FROM  INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='ncash_ref' AND TABLE_NAME like'temp_%'";
		List<Object[]> result = executeQuery( tempTableQuery, new ColumnDataType[]
		{ ColumnDataType.String } );
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];
			dropTable( tableName ,false);
		}

	}

	public Schema getViewSchema( String database, String table, boolean syncWithNonncash ) throws DataSourceException
	{
		// create the schema and database
		Schema dbSchema = new Schema();
		Database dbDatabase = new Database();
		dbDatabase.DatabaseName = database;

		dbSchema.Databases.add( dbDatabase );

		List<Object[]> result = executeQuery( getAllViewDetailsQuery( database, table ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Int, ColumnDataType.Bool, ColumnDataType.Int, ColumnDataType.Int } );

		Table newTable = null;
		boolean invalidTable = false;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];
			String columnName = ( String ) row[1];
			String dataType = ( String ) row[2];
			Integer length = ( Integer ) row[3];
			Boolean isNullable = ( Boolean ) row[4];
			Integer sequenceNumber = ( Integer ) row[5];
			Integer scale = ( Integer ) row[6];

			// table changed name so make a new one
			if ( newTable == null || !newTable.TableName.equals( tableName ) )
			{
				if ( newTable != null )
				{
					if ( invalidTable )
					{
						log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
					}
					else
					{
						dbDatabase.Tables.add( newTable );
					}
				}

				invalidTable = false;
				newTable = new Table();
				newTable.TableName = tableName;
				newTable.database = dbDatabase;

			}

			// create and populate a column
			Column column = new Column();
			column.ColumnName = columnName;

			// if length row is null (i.e typically on a text/clob data-type, dont perform unkownToInteger on it
			// (which cannot accept nulls) -- just set it to -1
			if ( length != null )
				column.Length = length;
			else
				column.Length = -1;

			column.DataType = dbTypeToColumnDataType( dataType, column.Length, scale, syncWithNonncash );

			// if we get a unknown column then ignore this table
			if ( column.DataType == null )
				invalidTable = true;

			// Text type is unlimited in length, so returns 'null' - avoid calling unknownToInteger on it
			column.IsMandatory = !isNullable;
			column.SequenceNumber = sequenceNumber;
			column.table = newTable;

			newTable.Columns.add( column );
		}

		if ( newTable != null )
		{
			if ( invalidTable )
			{
				log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
			}
			else
			{
				dbDatabase.Tables.add( newTable );
			}
		}

		// get the list of indexes
		result = executeQuery( getAllIndexDetailsQuery( database, table ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Int } );

		Index index = null;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];
			String indexName = ( String ) row[1];
			String columnName = ( String ) row[2];
			Boolean isClustered = ( Boolean ) row[3];
			Boolean isUnique = ( Boolean ) row[4];
			Boolean isIgnoreDupKey = ( Boolean ) row[5];

			// find the table for this index
			Table currentTable = dbDatabase.findTable( tableName );

			// skip if the table was not matched, may have been ignored above
			if ( currentTable == null )
				continue;

			// index/table changed name so make a new one
			if ( index == null || !index.IndexName.equals( indexName ) || !index.table.TableName.equals( currentTable.TableName ) )
			{
				index = new Index();
				index.IndexName = indexName;
				index.IsClustered = isClustered;
				index.IsUnique = isUnique;
				index.IsIgnoreDupKey = isIgnoreDupKey;

				index.table = currentTable;
				currentTable.Indexes.add( index );
			}

			// add the column name to the index
			index.addColumn( columnName );
		}

		return dbSchema;
	}

	public void updateNextNumber( Table table, String objectName ) throws DataSourceException
	{
		updateNextNumber( table.TableName, table.getPrimaryKeyColumn().ColumnName, objectName );
	}

	public void updateNextNumber( String tableName, String primaryKeyColumn, String objectName ) throws DataSourceException
	{
		String selectMax = "select max( " + primaryKeyColumn + " ) from " + tableName;
		List<Object[]> tableMax = executeQuery( selectMax, new ColumnDataType[]
		{ ColumnDataType.Int } );
		Object maxValue = tableMax.get( 0 )[0];

		if ( maxValue == null )
		{
			maxValue = new Integer( 0 );
		}

		if ( "PartitionTbl".equals( objectName ) && maxValue.equals( 0 ) )
			maxValue = new Integer( -1 );

		String update = "update next_object_no set non_current_no = " + maxValue + " where non_object_name = '" + objectName + "'";
		int updateCount = execute( update );

		// if there was no update do an insert
		if ( updateCount < 1 )
		{
			String insert = "insert into next_object_no (non_current_no, non_object_name ) values (" + maxValue + ", '" + objectName + "' )";
			execute( insert );
		}
	}

	public Table getTableWithoutIndexes( String database, String table ) throws DataSourceException
	{
		List<Object[]> result = executeQuery( getAllTableDetailsQuery( database, table ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Int, ColumnDataType.Bool, ColumnDataType.Int, ColumnDataType.Int } );

		Table newTable = null;
		boolean invalidTable = false;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( ( String ) row[0] ).toLowerCase();
			String columnName = ( ( String ) row[1] ).toLowerCase();
			String dataType = ( String ) row[2];
			Integer length = ( Integer ) row[3];
			Boolean isNullable = ( Boolean ) row[4];

			// table changed name so make a new one
			if ( newTable == null || !newTable.getTableName().equals( tableName ) )
			{
				if ( newTable != null )
				{
					if ( invalidTable )
					{
						log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.getTableName() ) );
					}
				}

				invalidTable = false;

				//                newTable = new Table( tableName, SchemaHelper.getTblPrefixFromCol(columnName));

				newTable = new Table( tableName, columnName.substring( 0, 3 ) );
			}

			// if length row is null (i.e typically on a text/clob data-type, dont perform unkownToInteger on it
			// (which cannot accept nulls) -- just set it to -1
			if ( length == null )
				length = -1;

			// Text type is unlimited in length, so returns 'null' - avoid calling unknownToInteger on it

			// create and populate a column
			Column column = new Column( columnName, dbTypeToColumnDataType( dataType, length ), length, !isNullable, newTable );

			// if we get a unknown column then ignore this table
			if ( column.getDataType() == null )
				invalidTable = true;

			newTable.getColumns().add( column );
		}

		if ( newTable != null )
		{
			if ( invalidTable )
			{
				log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.getTableName() ) );
			}
		}

		return newTable;
	}

	public Schema getSchemaForNonNcashDB( String database, String table ) throws DataSourceException, HibernateException
	{
		// create the schema and database
		Schema dbSchema = new Schema();
		Database dbDatabase = new Database();
		dbDatabase.DatabaseName = database;

		dbSchema.Databases.add( dbDatabase );

		List<Object[]> result = executeQuery( getAllTableDetailsQuery( database, table, true ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Int, ColumnDataType.Bool, ColumnDataType.Int, ColumnDataType.Int } );

		Table newTable = null;
		boolean invalidTable = false;

		List<String> tableDfnNames = HibernateSession.find( "select tin.TinTableName from TableInst tin" );
		//internal tables created by application
		for ( int i = 0; i < INTERNAL_TBLS.length; i++ )
		{
			tableDfnNames.add( INTERNAL_TBLS[i] );
		}
		List<String> auditingTbls = HibernateSession.find( "select aul.EntityTbl.EntEntity from AuditLevel aul" );

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];

			//skip tables which have entry in table definition and internal tables created by application
			if ( tableDfnNames.contains( tableName.toLowerCase() ) )
				continue;

			//skip auditing tables
			if ( tableName.endsWith( "_adt" ) || tableName.endsWith( "_ADT" ) )
			{
				String tblName = tableName.substring( 0, tableName.length() - 4 ).toLowerCase();
				if ( auditingTbls.contains( StringHelper.underScoreToCamelCase( tblName ) ) )
					continue;
			}

			String columnName = ( String ) row[1];
			String dataType = ( String ) row[2];
			Integer length = ( Integer ) row[3];
			Boolean isNullable = ( Boolean ) row[4];
			Integer sequenceNumber = ( Integer ) row[5];

			// table changed name so make a new one
			if ( newTable == null || !newTable.TableName.equals( tableName ) )
			{
				if ( newTable != null )
				{
					if ( invalidTable )
					{
						log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
					}
					else
					{
						dbDatabase.Tables.add( newTable );
					}
				}

				invalidTable = false;
				newTable = new Table();
				newTable.TableName = tableName;
				newTable.database = dbDatabase;

			}

			// create and populate a column
			Column column = new Column();
			column.ColumnName = columnName;

			// if length row is null (i.e typically on a text/clob data-type, dont perform unkownToInteger on it
			// (which cannot accept nulls) -- just set it to -1
			if ( length != null )
				column.Length = length;
			else
				column.Length = -1;

			column.DataType = dbTypeToColumnDataType( dataType, column.Length );

			// if we get a unknown column then ignore this table
			if ( column.DataType == null )
				invalidTable = true;

			// Text type is unlimited in length, so returns 'null' - avoid calling unknownToInteger on it
			column.IsMandatory = !isNullable;
			column.SequenceNumber = sequenceNumber;
			column.table = newTable;

			newTable.Columns.add( column );
		}

		if ( newTable != null )
		{
			if ( invalidTable )
			{
				log.warn( StringUtil.create( "Skipping table '%1' with invalid datatypes", newTable.TableName ) );
			}
			else
			{
				dbDatabase.Tables.add( newTable );
			}
		}

		if ( table == null )
			return dbSchema;

		// get the list of indexes
		result = executeQuery( getAllIndexDetailsQuery( database, table, true ), new ColumnDataType[]
		{ ColumnDataType.String, ColumnDataType.String, ColumnDataType.String, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Bool, ColumnDataType.Int } );

		Index index = null;

		// iterate through all the table populating them
		for ( Object[] row : result )
		{
			// extract the fields
			String tableName = ( String ) row[0];

			if ( tableDfnNames.contains( tableName ) )
				continue;

			String indexName = ( String ) row[1];
			String columnName = ( String ) row[2];
			Boolean isClustered = ( Boolean ) row[3];
			Boolean isUnique = ( Boolean ) row[4];
			Boolean isIgnoreDupKey = ( Boolean ) row[5];

			// find the table for this index
			Table currentTable = dbDatabase.findTable( tableName );

			// skip if the table was not matched, may have been ignored above
			if ( currentTable == null )
				continue;

			// index/table changed name so make a new one
			if ( index == null || !index.IndexName.equals( indexName ) || !index.table.TableName.equals( currentTable.TableName ) )
			{
				index = new Index();
				index.IndexName = indexName;
				index.IsClustered = isClustered;
				index.IsUnique = isUnique;
				index.IsIgnoreDupKey = isIgnoreDupKey;

				index.table = currentTable;
				currentTable.Indexes.add( index );
			}

			// add the column name to the index
			index.addColumn( columnName );
		}

		return dbSchema;
	}

	public void updateAllNextNumber() throws DataSourceException
	{
		for ( Database database : schema.Databases )
		{
			for ( Table table : database.Tables )
			{
				updateNextNumber( table, StringHelper.underScoreToCamelCase( table.TableName ) );
			}
		}
	}

	public List<String> getDropTableDDL( Table table ) throws DataSourceException
	{
		return getDropTableDDL( table, true );
	}

	public List<String> getDropIndexDDL( Index index ) throws DataSourceException
	{
		return getDropIndexDDL( index, true );
	}

	public boolean isUnicode()
	{
		return isUnicode;
	}

	protected void executeDDLQueryWithWait( String query, Connection conn ) throws DataSourceException
	{
		Statement stmt = null;

		try
		{
			stmt = conn.createStatement();
		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}

		DataSourceException returnException = null;
		//get start time in millis to check for the expire period later
		long startMillis = new DateTime().getMillis();

		while ( true )
		{
			try
			{
				stmt.executeQuery( query );
			}
			catch ( SQLException e )
			{
				//if resource busy exception occurs, then keep trying to execute the query for 'ddlWaitSeconds'
				//if we don't get the lock even after 'ddlWaitSeconds', then throw a custom exception.
				//if it's not a resource busy exception, just wrap the exception and throw it.
				if ( isResourceBusyErrorCode( e.getErrorCode() ) )
				{
					if ( ( new DateTime().getMillis() - startMillis ) / 1000 <= getDDLWaitSeconds() )
						continue;

					returnException = new DataSourceException( "Query: " + query.toUpperCase() + " failed after " + getDDLWaitSeconds() + " seconds due to a Table level Lock being held by another process" );
				}
				else
				{
					returnException = new DataSourceException( e );
				}
			}

			if ( stmt != null )
			{
				try
				{
					stmt.close();
				}
				catch ( SQLException e )
				{
					log.error( e );
				}
			}

			if ( returnException != null )
				throw returnException;

			//if no exception occurred or we got hold of the lock for the ddl, break
			break;
		}
	}

	protected boolean isResourceBusyErrorCode( int errorCode )
	{
		return false;
	}

	protected long getDDLWaitSeconds()
	{
		return 0;
	}

	public void truncateTable( String database, String tableName, boolean executeWithWait ) throws DataSourceException
	{
		truncateTable( database, tableName );
	}

	protected List<Object[]> executeQuery( String query, ColumnDataType[] resultTypes ) throws DataSourceException
	{
		List<Object[]> result = null;

		Connection conn = null;
		try
		{
			if ( query != null )
			{
				// get a connection from the session
				conn = getConnection();
				result = executeQuery( conn, query, resultTypes );
			}
			else
				return result = new ArrayList<Object[]>();

		}
		finally
		{
			if ( conn != null )
			{
				try
				{
					conn.close();
				}
				catch ( Exception e )
				{
				}
			}
		}

		return result;
	}

	protected List<Object[]> executeQuery( Connection conn, String query, ColumnDataType[] resultTypes ) throws DataSourceException
	{
		List<Object[]> result = null;

		// create the statement and execute the query
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery( query );
			result = DataSourceHelper.unwrapResultSet( rs, resultTypes );
			conn.commit();
		}
		catch ( SQLException e )
		{
			try
			{
				conn.rollback();
			}
			catch ( SQLException e1 )
			{
				throw new DataSourceException( e1 );
			}
			throw new DataSourceException( e );
		}
		finally
		{
			if ( stmt != null )
			{
				try
				{
					stmt.close();
				}
				catch ( SQLException e )
				{
					// ignore
				}
			}
		}

		return result;
	}

	protected int execute( Connection conn, String query ) throws DataSourceException
	{
		int result = -1;

		// create the statement and execute the query
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			if ( !stmt.execute( query ) )
			{
				result = stmt.getUpdateCount();
			}
		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}
		finally
		{
			if ( stmt != null )
			{
				try
				{
					stmt.close();
				}
				catch ( SQLException e )
				{
					// ignore
				}
			}
		}

		return result;
	}

	protected int execute( String query ) throws DataSourceException
	{
		int result = -1;

		Connection conn = null;
		try
		{
			conn = getConnection();

			// create the statement and execute the query
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				if ( !stmt.execute( query ) )
				{
					result = stmt.getUpdateCount();
				}
				conn.commit();
			}
			catch ( SQLException e )
			{
				try
				{
					conn.rollback();
				}
				catch ( SQLException e1 )
				{
					throw new DataSourceException( e1 );
				}
				throw new DataSourceException( e );
			}
			finally
			{
				if ( stmt != null )
				{
					try
					{
						stmt.close();
					}
					catch ( SQLException e )
					{
						// ignore
					}
				}
			}
		}
		finally
		{
			if ( conn != null )
			{
				try
				{
					conn.close();
				}
				catch ( Exception e )
				{
				}
			}
		}

		return result;
	}

	public int executeUpdate( String query, Object... params ) throws DataSourceException
	{
		int result = -1;

		Connection conn = null;
		try
		{
			conn = getConnection();

			// create the statement and execute the query
			PreparedStatement stmt = null;
			try
			{
				stmt = conn.prepareStatement( query );

				int index = 1;
				for ( Object param : params )
				{
					stmt.setObject( index, param );
					index++;
				}

				result = stmt.getUpdateCount();
				conn.commit();
			}
			catch ( SQLException e )
			{
				try
				{
					conn.rollback();
				}
				catch ( SQLException e1 )
				{
					throw new DataSourceException( e1 );
				}
				throw new DataSourceException( e );
			}
			finally
			{
				if ( stmt != null )
				{
					try
					{
						stmt.close();
					}
					catch ( SQLException e )
					{
						// ignore
					}
				}
			}
		}
		finally
		{
			if ( conn != null )
			{
				try
				{
					conn.close();
				}
				catch ( Exception e )
				{
				}
			}
		}

		return result;
	}

	public void executeBatch( Connection conn, Statement stmt, ArrayList<String> queriesList ) throws DataSourceException
	{
		try
		{
			stmt = conn.createStatement();
			for ( String sql : queriesList )
			{
				stmt.addBatch( sql );
			}
			try
			{
				stmt.executeBatch();
			}
			catch ( Exception e )
			{
				throw new SQLException( e );
			}
		}
		catch ( SQLException e )
		{
			try
			{
				if ( e.getCause() != null && e.getCause() instanceof BatchUpdateException )
					log.error( ( ( BatchUpdateException ) e.getCause() ).getNextException() );
				if ( conn != null )
					conn.rollback();
			}
			catch ( SQLException e1 )
			{
				throw new DataSourceException( e1 );
			}
			throw new DataSourceException( "Error executing sql: %1", e, queriesList.toString() );
		}
	}

	public int[] executeList( List<String> sqlList ) throws DataSourceException
	{
		int[] results = null;

		Connection conn = null;
		try
		{
			conn = getConnection();

			// create the statement and execute the query
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				for ( String sql : sqlList )
				{
					stmt.addBatch( sql );
				}
				try
				{
					results = stmt.executeBatch();
				}
				catch ( Exception e )
				{
					throw new SQLException( e );
				}

				conn.commit();
			}
			catch ( SQLException e )
			{
				log.error( "Error executing sql: " + sqlList.toString(), e );
				try
				{
					if ( e.getCause() != null && e.getCause() instanceof BatchUpdateException )
						log.error( ( ( BatchUpdateException ) e.getCause() ).getNextException() );
					if ( conn != null )
						conn.rollback();
				}
				catch ( SQLException e1 )
				{
					throw new DataSourceException( e1 );
				}
				throw new DataSourceException( "Error executing sql: %1", e, sqlList.toString() );
			}
			finally
			{
				if ( stmt != null )
					try
					{
						stmt.close();
					}
					catch ( SQLException e )
					{
						throw new DataSourceException( e );
					}
			}
		}
		finally
		{
			if ( conn != null )
			{
				try
				{
					conn.close();
				}
				catch ( Exception e )
				{
				}
			}
		}

		return results;
	}

	public void createTable( Table table, String dataLocation, String indexLocation ) throws DataSourceException
	{
		// create the indexes by default
		createTable( table, dataLocation, indexLocation, true );
	}

	public void alterTableWithNoLogging( Table table ) throws DataSourceException
	{
		// create the table
		executeList( getAlterTableDMLWithNoLogging( table ) );

	}

	public void createTable( Table table, String dataLocation, String indexLocation, boolean createIndexes ) throws DataSourceException
	{
		// validate table before creation
		SchemaHelper.validateTable( table );

		// create the table
		executeList( getCreateTableDDL( table, dataLocation ) );

		// create the indexes
		if ( createIndexes )
			applyAfterTableCreateIndexes( table, dataLocation, indexLocation );
	}

	protected List<String> getAlterTableDMLWithNoLogging( Table table ) throws DataSourceException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}

	public void dropTableWarn( Table table )
	{
		// drop the table logging any exception
		try
		{
			dropTable( table );
		}
		catch ( DataSourceException e )
		{
			log.warn( StringUtil.create( "Failed to drop table '%1'", table.TableName ), e );
		}
	}

	public void dropTableSilent( Table table )
	{
		// drop the table swallowing any exception
		try
		{
			dropTable( table );
		}
		catch ( DataSourceException e )
		{

		}
	}

	public void dropTable( String tableName ) throws DataSourceException
	{
		executeList( getDropTableDDL( tableName, true ) );
	}
	
	public void dropTable( String tableName ,boolean ifExistCheck) throws DataSourceException
	{
		executeList( getDropTableDDL( tableName, ifExistCheck ) );
	}

	public void dropTableSilent( String tableName )
	{
		// drop the table swallowing any exception
		try
		{
			dropTable( tableName );
		}
		catch ( DataSourceException e )
		{
			e.printStackTrace();
		}
	}

	public void recreateTable( Table table, String dataLocation, String indexLocation ) throws DataSourceException
	{
		dropTableSilent( table );
		createTable( table, dataLocation, indexLocation );
	}

	public void dropTable( Table table ) throws DataSourceException
	{
		executeList( getDropTableDDL( table ) );
	}
	
	public void dropTable( Table table,boolean ifExist ) throws DataSourceException
	{
		executeList( getDropTableDDL( table,ifExist ) );
	}

	public void dropAllIndexes( List<Index> indexes ) throws DataSourceException
	{
		// drop the non-clustered indexes
		for ( Index index : indexes )
		{
			if ( !index.IsClustered )
			{
				List<String> sqlList = getDropIndexDDL( index );

				executeList( sqlList );
			}
		}

		// drop the clustered index
		for ( Index index : indexes )
		{
			if ( index.IsClustered )
			{
				List<String> sqlList = getDropIndexDDL( index );

				executeList( sqlList );

				break;
			}
		}
	}

	public void dropAllIndexes( Table table ) throws DataSourceException
	{
		dropAllIndexes( table.Indexes );
	}

	public void dropAllIndexes() throws DataSourceException
	{
		// get the current schema
		Schema currentSchema = getSchema( this.database );

		Database database = currentSchema.findDatabase( this.database );

		// loop over all the expected tables
		for ( Table table : database.Tables )
		{
			List<Index> indexesToDrop = new ArrayList<Index>();

			// loop over all indexes
			for ( Index index : table.Indexes )
			{
				indexesToDrop.add( index );
			}

			// apply the indexes
			dropAllIndexes( indexesToDrop );
		}

	}

	public void applyIndexes( Table table, String indexLocation ) throws DataSourceException
	{
		applyIndexes( table, indexLocation, true );
	}

	public void applyIndexes( String indexLocation ) throws DataSourceException
	{
		Database database = schema.findDatabase( this.database );

		Schema currentSchema = getSchema( this.database );

		// loop over all the expected tables
		for ( Table table : database.Tables )
		{
			// find the corresponding existing table
			Table currentTable = currentSchema.findTable( this.database, table.TableName );

			// table doesn't exist skip
			if ( currentTable == null )
				continue;

			List<Index> indexesToApply = new ArrayList<Index>();

			// loop over all indexes
			for ( Index index : table.Indexes )
			{
				// add the index to the apply list if it doesn't exist
				Index currentIndex = currentTable.findIndex( index.IndexName );

				if ( currentIndex == null )
				{
					indexesToApply.add( index );
				}
			}

			// apply the indexes
			applyIndexes( indexesToApply, indexLocation, true );
		}
	}

	public void applyIndexes( List<Index> indexes, String indexLocation, boolean applyClustered ) throws DataSourceException
	{
		if ( applyClustered )
		{
			for ( Index index : indexes )
			{
				if ( index.IsClustered )
				{
					applyIndex( index, indexLocation );

					break;
				}
			}
		}

		// apply the non-clustered indexes
		for ( Index index : indexes )
		{
			if ( !index.IsClustered )
			{
				applyIndex( index, indexLocation );
			}
		}
	}

	public void applyIndex( Index index, String indexLocation ) throws DataSourceException
	{
		// validate the index first
		SchemaHelper.validateIndex( index );

		List<String> sqlList = getCreateIndexDDL( index, indexLocation );
		executeList( sqlList );
	}

	public void applyIndexes( Table table, String indexLocation, boolean applyClustered ) throws DataSourceException
	{
		// apply the clustered index first
		applyIndexes( table.Indexes, indexLocation, applyClustered );
	}

	public List<String> getTableNames( String database ) throws DataSourceException
	{
		return getTableNames( database, "%" );
	}

	public List<String> getTableNames( String database, String tablePattern ) throws DataSourceException
	{
		String query = getTableQuery( database, tablePattern, false );

		Connection connection = null;
		List<String> tableNames = null;
		try
		{
			connection = getConnection();
			Statement stmt = null;
			ResultSet rs = null;
			try
			{
				stmt = connection.createStatement();
				rs = stmt.executeQuery( query );
				tableNames = new ArrayList<String>();

				while ( rs.next() )
				{
					String tableName = rs.getString( 1 );
					tableNames.add( tableName );
				}
			}
			finally
			{
				try
				{
					if ( rs != null )
						rs.close();
					if ( stmt != null )
						stmt.close();
				}
				catch ( SQLException e )
				{
					log.error( "Failed to clean up statement/result set", e );
				}
			}
		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}
		finally
		{
			if ( connection != null )
			{
				try
				{
					connection.close();
				}
				catch ( Exception e )
				{
					// ignore
				}
			}
		}

		return tableNames;
	}

	public void updateTable( Table oldTable, Table newTable, String dataLocation ) throws DataSourceException
	{
		List<String> sqlList = new ArrayList<String>();
		String statement;

		// Add use database header
		statement = getSelectDatabaseDDL( oldTable.database.DatabaseName );

		if ( statement != null && statement.length() > 0 )
			sqlList.add( statement );

		Boolean tbdSystemFl = null;
		try
		{
			tbdSystemFl = HibernateSession.queryExpectOneRow( "select tin.TableDfn.TbdSystemFl from TableInst tin where tin.TinTableName = :tableName", "tableName", oldTable.TableName );
		}
		catch ( HibernateException e1 )
		{
		}

		if ( oldTable.Columns.size() < newTable.Columns.size() && null != tbdSystemFl && !tbdSystemFl )
		{
			List<String> alterSqlList = getAlterTableDDL( oldTable, newTable, dataLocation );

			// Use alter statement instead creating temp table
			if ( alterSqlList != null )
			{
				sqlList.addAll( alterSqlList );
				executeList( sqlList );
				return;
			}
		}

		// Create a temporary migration table based on the new table and add to the schema
		Table migrationTable = null;
		try
		{
			migrationTable = newTable.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new DataSourceException( "Clone of table failed", e );
		}

		migrationTable.TableName = getMigrationName( newTable );

		// Make sure the migration table doesn't already exist and add to the schema
		if ( newTable.database.findTable( migrationTable.TableName ) != null )
			throw new DataSourceException( "The schema already contains the migration table '%1'", migrationTable.TableName );

		newTable.database.Tables.add( migrationTable );
		migrationTable.database = newTable.database;

		updateMigrationTable( migrationTable );

		// Build the sql for the migration table
		List<String> sql = getCreateTableDDL( migrationTable, dataLocation );
		sqlList.addAll( sql );

		// Step 1 execute
		executeList( sqlList );

		// Move the rows to the migration table
		// Create the select query handling NULL -> NOT NULL changes
		sqlList.clear();

		StringBuilder migrationTableSQ = new StringBuilder();
		migrationTableSQ.append( "select " );

		String primaryColumn = null;
		for ( int i = 0; i < newTable.Columns.size(); i++ )
		{
			// Get the next column
			Column newColumn = newTable.Columns.get( i );
			String selectColumn = "";

			if ( newColumn.IsPrimaryKey )
			{
				primaryColumn = newColumn.getColumnName();
			}
			// See if this column exists in the old table or if its definition has changed
			if ( oldTable.findColumn( newColumn.ColumnName ) != null )
			{
				// If the column is the same just migrate the data otherwise handle NULL -> NOT NULL
				Column oldColumn = oldTable.findColumn( newColumn.ColumnName );
				if ( ( oldColumn.IsMandatory == newColumn.IsMandatory ) || ( !newColumn.IsMandatory ) )
				{
					// Migrate the old data
					selectColumn = newColumn.ColumnName;
				}
				else
				{
					// Migrate the old data catering for possible NULL values that must be defaulted
					selectColumn = getFunctionName( "isnull" ) + "(" + newColumn.ColumnName + ", " + getDefaultConstant( newColumn ) + ")";
				}
				selectColumn = doSqlConvert( newColumn, selectColumn, oldColumn );
			}
			else
			{
				// If mandatory put the default value in otherwise use NULL
				if ( newColumn.IsMandatory )
				{
					//                    // Use the default value
					//                    selectColumn = getDefaultConstant( newColumn );

					String newColumnValue = null;
					boolean indexColumn = false;
					for ( Index index : newTable.getIndexes() )
					{
						if ( index.isIndexColumn( newColumn.ColumnName ) )
						{
							indexColumn = true;
						}
					}

					if ( indexColumn )
					{
						newColumnValue = primaryColumn;

						newColumnValue = getValueForNewIndexColumn( newColumn, newColumnValue );
					}
					else// Use the default value
					{
						newColumnValue = getDefaultConstant( newColumn );
					}

					selectColumn = newColumnValue;
				}
				else
				{
					// Just use NULL
					selectColumn = "NULL";
				}
			}

			migrationTableSQ.append( selectColumn + ", " );
		}

		// Remove last ", " and add the 'from' clause
		if ( newTable.Columns.size() > 0 )
			migrationTableSQ.delete( migrationTableSQ.length() - 2, migrationTableSQ.length() );
		migrationTableSQ.append( " from " + oldTable.TableName );

		// Copy the rows from the old table into the migration table
		String migrationTableIQ = "insert into " + migrationTable.TableName + "(" + StringHelper.merge( migrationTable.getColumnNames(), "," ) + ")" + StringHelper.NEW_LINE + migrationTableSQ.toString();

		// Build the sql for the copy of rows to the migration table
		sqlList.add( migrationTableIQ );

		// execute STEP 2
		try
		{
			executeList( sqlList );
		}
		catch ( DataSourceException e )
		{
			// on failure drop the migration table
			List<String> dropDDL = getDropTableDDL( migrationTable );

			executeList( dropDDL );

			throw e;
		}

		// Drop the old table
		sqlList.clear();
		sqlList.addAll( getDropTableDDL( oldTable, false ) );

		// execute STEP 3
		executeList( sqlList );

		// remove the migration table from the schema
		migrationTable.database.removeTable( migrationTable.TableName );

		// instead of creating a new table and copying just rename the temp table
		execute( getRenameTableDDL( migrationTable.TableName, newTable.TableName ) );

		// reapply the indexes
		//applyAfterTableCreateIndexes( newTable, dataLocation, indexLocation );
	}

	protected String doSqlConvert( Column newColumn, String selectColumn, Column oldColumn ) throws DataSourceException
	{
		if ( !oldColumn.DataType.equals( newColumn.DataType ) )
		{
			selectColumn = getSqlConvert( newColumn.DataType, newColumn.Length, oldColumn );
		}
		return selectColumn;
	}

	protected String getValueForNewIndexColumn( Column newColumn, String newColumnValue )
	{
		return newColumnValue;
	}

	protected void updateMigrationTable( Table migrationTable )
	{

	}

	public void updateIndex( Index oldIndex, Index newIndex, String indexLocation ) throws DataSourceException
	{
		List<String> sqlList = new ArrayList<String>();

		if ( oldIndex == null )
		{
			// creating new index
			sqlList.addAll( getCreateIndexDDL( newIndex, indexLocation ) );
		}
		else if ( newIndex == null )
		{
			// dropping old index
			sqlList.addAll( getDropIndexDDL( oldIndex, true ) );
		}
		else
		{
			// updating existing index - drop and recreate
			sqlList.addAll( getDropIndexDDL( oldIndex, true ) );
			sqlList.addAll( getCreateIndexDDL( newIndex, indexLocation ) );
		}

		// execute the sql to update the index
		executeList( sqlList );
	}

	public void renameTable( String oldTableName, String newTableName ) throws DataSourceException
	{
		execute( getRenameTableDDL( oldTableName, newTableName ) );
	}

	public void renameColumn( String tableName, String oldColumnName, String newColumnName ) throws DataSourceException
	{
		execute( getColumnRenameDDL( tableName, oldColumnName, newColumnName ) );
	}

	public void addNewColumn( String table, String newColumn, String dataType ) throws DataSourceException
	{
		execute( getAddNewColumnDDL( table, newColumn, dataType ) );
	}

	public void dropColumn( String table, String column ) throws DataSourceException
	{
		execute( getDropColumnDDL( table, column ) );
	}

	protected String getDropColumnDDL( String table, String column )
	{
		return "alter table " + table + " drop column " + column;
	}

	public void copyTable( Table oldTable, String newTableName ) throws DataSourceException
	{
		try
		{
			Table newTable = oldTable.clone();
			newTable.TableName = newTableName;

			// don't copy indexes
			newTable.Indexes.clear();

			// create the table
			createTable( newTable, null, null );

			// copy the rows
			execute( "insert into " + newTableName + " select * from " + oldTable.TableName );
		}
		catch ( CloneNotSupportedException e )
		{
			throw new DataSourceException( e );
		}
	}

	public boolean tableExists( String tableName ) throws DataSourceException
	{
		List<Object[]> results = executeQuery( getTableQuery( database, tableName, true ), new ColumnDataType[]
		{ ColumnDataType.String } );

		return results.size() > 0;
	}

	public String getSelectString( String tableName, String[] colNames, long min, long max )
	{
		return "SELECT " + StringHelper.merge( colNames, ", " ) + " FROM ( SELECT " + tableName + ".*, ROWNUM  AS ROWNO FROM " + tableName + ") TMP WHERE TMP.ROWNO between 0 " + "and " + max;
	}

	public String getSelectString( String tableName, String[] colNames, String orderByClause, long min, long max )
	{
		return "SELECT " + StringHelper.merge( colNames, ", " ) + " FROM ( SELECT " + tableName + ".*, ROW_NUMBER() OVER (ORDER BY " + orderByClause + ") AS ROWNO FROM " + tableName + ") TMP WHERE TMP.ROWNO between " + min + " and " + max;
	}

	public String getSelectString( String tableName, String[] colNames, String orderByClause, String whereClause, long min, long max )
	{
		return "SELECT " + StringHelper.merge( colNames, ", " ) + " FROM ( SELECT " + tableName + ".*, ROW_NUMBER() OVER (ORDER BY " + orderByClause + ") AS ROWNO FROM " + tableName + " where " + whereClause + " ) TMP WHERE TMP.ROWNO between " + min + " and " + max;
	}

	public String getPaginatedSelectString( String tableName, String[] colNames, long batchSize )
	{
		return "SELECT " + StringHelper.merge( colNames, ", " ) + " FROM " + tableName + " WHERE ROWNUM <= " + batchSize;
	}

	public String getPaginatedSelectStringWithOrderBy( String tableName, String[] colNames, String[] orderByColumns, long batchSize )
	{
		return "SELECT " + StringHelper.merge( colNames, ", " ) + " FROM " + "( SELECT " + StringHelper.merge( colNames, ", " ) + " FROM " + tableName + " ORDER BY " + StringHelper.merge( orderByColumns, ", " ) + ") WHERE ROWNUM <= " + batchSize;
	}

	protected Table createTempTable( Table table ) throws DataSourceException
	{
		SchemaHelper.validateTable( table );

		// Create a temporary migration table based on the new table and add to the schema
		Table migrationTable = null;
		try
		{
			migrationTable = table.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new DataSourceException( "Clone of table failed", e );
		}

		migrationTable.TableName = getMigrationName( table );

		// Make sure the migration table doesn't already exist and add to the schema
		if ( table.database.findTable( migrationTable.TableName ) != null )
			throw new DataSourceException( "The schema already contains the migration table '%1'", migrationTable.TableName );

		return migrationTable;
	}

	protected void onPostUpdateTable( Table table ) throws DataSourceException
	{

	}

	protected long getSuccessRecordCount( String line )
	{
		return -1;
	}

	protected void checkBCPOutputError( String line )
	{
	}

	public static Map<String, String> getStoredProcedureMacros( String datasourceName ) throws DataSourceException
	{
		Map<String, String> macroMap = new HashMap<String, String>();

		try
		{
			//getClass()
			Document doc = XMLHelper.loadDocument( AbstractDataSource.class.getResourceAsStream( "stored_procedure_macros.xml" ) );

			Element rootElement = doc.getRootElement();

			// search for the specified datasource name
			Element dataSourceElement = null;
			for ( Object dsElement : rootElement.elements() )
			{
				if ( ( ( Element ) dsElement ).attributeValue( "Datasource" ).equals( datasourceName ) )
				{
					dataSourceElement = ( Element ) dsElement;
					break;
				}
			}

			if ( dataSourceElement == null )
				throw new DataSourceException( "Datasource name '%1' not found in stored_procedure_macros.xml", datasourceName );

			// get all the macros within the Macros tag
			Element macrosElement = ( Element ) dataSourceElement.elements( "Macros" ).get( 0 );

			for ( Object elObj : macrosElement.elements() )
			{
				Element macroElement = ( Element ) elObj;

				String str = macroElement.attributeValue( "String" );
				String value = macroElement.getText();

				// sanity checks
				if ( StringHelper.isEmpty( str ) )
					throw new DataSourceException( "Empty macro attribute found." );

				// add to the map
				macroMap.put( str, value );
			}

		}
		catch ( DocumentException e )
		{
			throw new DataSourceException( e );
		}

		// return the map of macros
		return macroMap;
	}

	public static String expandMacros( Map<String, String> macroMap, String inputString )
	{
		boolean madeChange = true;

		// we must keep processing until no more macro substitutions have occured
		while ( madeChange )
		{
			// initialise our changesMade boolean
			madeChange = false;

			// search for each macro in the map
			for ( Map.Entry<String, String> entry : macroMap.entrySet() )
			{
				String macro = entry.getKey();
				String value = entry.getValue();

				int startIndex = inputString.indexOf( macro );
				while ( startIndex >= 0 )
				{
					// get the index of the last character of the macro
					int endIndex;
					String inputMacro;
					String replaceString;

					if ( value.contains( "$" ) )
					{
						// the macro expects arguments - find the closing bracket
						endIndex = inputString.indexOf( ")", startIndex ) + 1;
						inputMacro = inputString.substring( startIndex, endIndex );

						// get just the args string
						String argsStr = inputMacro.substring( inputMacro.indexOf( "(" ) + 1, inputMacro.length() - 1 );

						// split the input macro on commas
						String[] args = StringHelper.split( argsStr, "," );

						// parse the string
						replaceString = StringHelper.parse( value, "$", args );
					}
					else
					{
						// no argumenets - direct substitution
						endIndex = startIndex + macro.length();
						inputMacro = macro;
						replaceString = value;
					}

					// now we have the string to replace the macro with
					StringBuilder sb = new StringBuilder();
					sb.append( inputString.substring( 0, startIndex ) );
					sb.append( replaceString );
					sb.append( inputString.substring( endIndex ) );

					// finalize the new input string
					inputString = sb.toString();

					// remember we need to run through the macros again
					madeChange = true;

					// get the next occurance of this macro
					startIndex = inputString.indexOf( macro );
				}
			}
		}

		return inputString;
	}

	public String createSQLUpdateQuery( String tableName, List<Column> setColumnList, List<Column> whereColumnList )
	{
		StringBuilder updateQuery = new StringBuilder();
		StringBuilder setClause = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();

		updateQuery.append( "update " ).append( tableName ).append( " " );
		setClause.append( "set " );

		for ( Column column : setColumnList )
		{
			Object value = column.getValue();
			String afterFormatting = "";
			setClause.append( column.getColumnName() ).append( "= " );
			if ( value == null )
			{
				afterFormatting = "null";
				setClause.append( afterFormatting ).append( "," );
				continue;
			}

			afterFormatting = formatValue( column, value );

			setClause.append( afterFormatting ).append( "," );
		}

		setClause.delete( setClause.length() - 1, setClause.length() );
		whereClause.append( " where " );

		for ( Column column : whereColumnList )
		{
			Object value = column.getValue();
			whereClause.append( column.getColumnName() );
			String afterFormatting = "";
			if ( value == null )
			{
				whereClause.append( " is " ).append( "null and " );
				continue;
			}

			whereClause.append( " = " );
			afterFormatting = formatValue( column, value );
			whereClause.append( afterFormatting ).append( " and " );
		}

		if ( whereClause.toString().endsWith( " and " ) )
			whereClause.delete( whereClause.length() - 5, whereClause.length() );

		String result = updateQuery.append( setClause ).append( whereClause ).toString();
		log.debug( result );
		return result;
	}

	public String createSQLInsertQuery( String table, List<Column> columns )
	{
		StringBuilder insertQuery = new StringBuilder( "Insert into " ).append( table ).append( " " );
		StringBuilder columnList = new StringBuilder();
		StringBuilder valueList = new StringBuilder();

		for ( Column column : columns )
		{
			Object value = column.getValue();
			String afterFormatting = "";

			columnList.append( column.getColumnName() ).append( " ," );
			if ( value == null )
			{
				valueList.append( "null" ).append( "," );
				continue;
			}

			afterFormatting = formatValue( column, value );
			valueList.append( afterFormatting ).append( "," );
		}

		columnList.deleteCharAt( columnList.length() - 1 );
		valueList.deleteCharAt( valueList.length() - 1 );

		String query = insertQuery.append( "( " ).append( columnList ).append( " )" ).append( "values  (" ).append( valueList ).append( ")" ).toString();
		log.debug( query );
		return query;
	}

	public String createSQLSelectQuery( String tableNames, List<Column> selectColumns, List<Column> whereColumns )
	{
		StringBuilder selectList = new StringBuilder();
		StringBuilder whereList = new StringBuilder();

		selectList.append( "select " );

		for ( Column column : selectColumns )
		{
			selectList.append( getColumnName( column ) + ", " );
		}

		selectList.delete( selectList.length() - 2, selectList.length() );

		selectList.append( " from " + tableNames );

		if ( whereColumns.isEmpty() )
			return selectList.toString();

		whereList.append( " where " );

		for ( Column column : whereColumns )
		{
			Object value = column.getValue();
			String afterFormatting = "";
			whereList.append( getColumnName( column ) );

			if ( value == null && column.hasValue() )
			{
				whereList.append( " is " ).append( "null and " );
				continue;
			}
			if ( column.hasValue() )
			{
				whereList.append( " = " );
				afterFormatting = formatValue( column, value );
				whereList.append( afterFormatting ).append( " and " );
			}
			else
			{
				whereList.append( " and " );
			}
		}

		if ( whereList.toString().endsWith( " and " ) )
			whereList.delete( whereList.length() - 5, whereList.length() );

		String selectQuery = selectList.append( whereList ).toString();

		log.debug( selectQuery );

		return selectQuery;
	}

	private String getColumnName( Column column )
	{
		if ( StringHelper.isEmpty( column.table.TablePrefix ) )
			return column.getColumnName();

		return column.table.TablePrefix + DOT + column.getColumnName();
	}

	private String formatValue( Column column, Object value )
	{
		String afterFormatting;
		if ( ColumnDataType.DateTime.equals( column.getDataType() ) )
		{
			afterFormatting = getDateTimeConversion( value );
		}
		else if ( ColumnDataType.Bool.equals( column.getDataType() ) )
		{
			if ( value instanceof Boolean )
			{
				Boolean bool = ( Boolean ) value;
				afterFormatting = "'" + ( value = bool ? "Y" : "N" ) + "'";
			}
			else
			{
				afterFormatting = "'" + ( ( value.toString().equalsIgnoreCase( "true" ) || "Y".equalsIgnoreCase( value.toString() ) ) ? "Y" : "N" ) + "'";
			}

		}
		else if ( ColumnDataType.String.equals( column.getDataType() ) )
		{
			String strValue = value.toString();
			value = strValue.indexOf( "'" ) > -1 ? strValue.replaceAll( "'", "''" ) : strValue;
			afterFormatting = "'" + value + "'";
		}
		else if ( ColumnDataType.Decimal.equals( column.getDataType() ) )
		{
			afterFormatting = "'" + ( ( BigDecimal ) value ).multiply( new BigDecimal( 1000000 ) ).longValue() + "'";
		}
		else
			afterFormatting = "'" + value + "'";
		return afterFormatting;
	}

	private static String ProcessFile( Map<String, String> map, BufferedReader reader ) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		while ( true )
		{
			String line = reader.readLine();
			if ( line == null )
				break;
			sb.append( line + "\r\n" );
		}
		return expandMacros( map, sb.toString() );
	}

	public String getIsNullFunctionName()
	{
		try
		{
			return getFunctionName( "isnull" );
		}
		catch ( DataSourceException e )
		{
			throw new NCashRuntimeException( e );
		}
	}

	public String getSelectString( String tableName, String[] colNames )
	{
		return "select " + StringHelper.merge( colNames, ", " ) + " from " + tableName + " where rownum = 1";
	}

	protected String getMigrationName( Table table )
	{
		// Return a migration name - temp_ limited to 30 chars
		return "temp_" + table.TableName.substring( 0, ( ( table.TableName.length() > 25 ) ? 25 : table.TableName.length() ) );
	}

	public String getCaseStatement( String column, String... conditions ) throws DataSourceException
	{
		//TODO: need to improve this method to include negative conditions.

		if ( conditions == null || conditions.length == 0 || ( conditions.length % 2 ) == 1 )
		{
			throw new DataSourceException( "Invalid number of parameters." );
		}

		String caseStmt = " CASE ";
		String whenStmt = " WHEN ";
		String thenStmt = " THEN ";
		String endStmt = " END ";

		String returnString = caseStmt;
		for ( int index = 0; index < conditions.length; index += 2 )
		{
			String operator = conditions[index] == null ? " IS " : " = ";
			String condition = conditions[index] == null ? " NULL " : conditions[index];
			String value = conditions[index + 1] == null ? " NULL " : conditions[index + 1];
			returnString = returnString + whenStmt + column + operator + condition + thenStmt + value;
		}

		returnString = returnString + endStmt;

		return returnString;
	}

	public String mod( String column, int divider )
	{
		String functionName = null;
		try
		{
			functionName = getFunctionName( "MOD" );
		}
		catch ( DataSourceException e )
		{

			functionName = "MOD";
		}
		return StringHelper.wrap( functionName, column, String.valueOf( divider ) );
	}

	public String toChar( String column, ColumnDataType dataType )
	{
		String functionName = null;
		try
		{
			functionName = getFunctionName( "CONVERT" );
		}
		catch ( DataSourceException e )
		{

			functionName = "TO_CAHR";
		}

		if ( ColumnDataType.String.equals( dataType ) )
		{
			return column;
		}
		else if ( ColumnDataType.DateTime.equals( dataType ) )
		{
			return StringHelper.wrap( functionName, column, "'" + NCashDateFormatter.dateFormatStringForMySQlStorage + "'" );
		}
		else
		{
			return StringHelper.wrap( functionName, column );
		}
	}

	public String hash( List<Column> columns )
	{

		List<String> columnNames = new ArrayList<String>();

		for ( Column column : columns )
		{
			String columName = toChar( column.getColumnName(), column.getDataType() );
			columnNames.add( columName );
		}

		String returnString = StringHelper.merge( columnNames, getConcatOperator() );

		try
		{
			returnString = StringHelper.wrap( getFunctionName( "ISNULL" ), returnString, "'null'" );
		}
		catch ( DataSourceException e )
		{
			returnString = StringHelper.wrap( "NVL", returnString, "'null'" );
		}

		returnString = wrapIntoBinaryChecksumFunction( returnString );

		try
		{
			returnString = StringHelper.wrap( getFunctionName( "ABS" ), returnString );
		}
		catch ( DataSourceException e )
		{
			returnString = StringHelper.wrap( "ABS", returnString );
		}

		return returnString;
	}

	protected String wrapIntoBinaryChecksumFunction( String returnString )
	{
		try
		{
			returnString = StringHelper.wrap( getFunctionName( "BINARY_CHECKSUM" ), returnString );
		}
		catch ( DataSourceException e )
		{
			returnString = StringHelper.wrap( "ORA_HASH", returnString );
		}

		return returnString;
	}

	public String createTableFromAnotherTable( String newTable, String oldTable, String[] colNames )
	{
		return "create table " + newTable + " as (select " + StringHelper.merge( colNames, ", " ) + " from " + oldTable + ")";
	}

	public String createTableFromAnotherTable( String newTable, String oldTable, String[] colNames, String whereClause )
	{
		String query = createTableFromAnotherTable( newTable, oldTable, colNames );
		return query.substring( 0, query.length() - 1 ) + " where " + whereClause + ")";
	}

	public String getNullSafeFormattedColumn( String columnName, ColumnDataType type )
	{
		String column = getFormattedColumn( columnName, type );

		return getCoalesceString( column, "'NULL'" );
	}

	public String getFormattedColumn( String columnName, ColumnDataType type )
	{
		String formattedColumn = columnName;

		if ( ColumnDataType.Bool.equals( type ) )
		{
			formattedColumn = "(CASE WHEN " + columnName + " = 'Y' THEN 'true' ELSE 'false' end)";
		}
		else if ( ColumnDataType.Decimal.equals( type ) )
		{
			formattedColumn = castNumberToFloat( columnName );
			formattedColumn = formattedColumn + " / 1000000 ";
			formattedColumn = castNumberToVarchar( formattedColumn );
		}
		else if ( ColumnDataType.DateTime.equals( type ) )
		{
			formattedColumn = toChar( columnName, type );
		}
		else if ( ColumnDataType.Long.equals( type ) || ColumnDataType.Int.equals( type ) || ColumnDataType.String.equals( type ) )
		{
			formattedColumn = castNumberToVarchar( columnName );
		}

		return formattedColumn;
	}

	protected String getCoalesceString( String columnName, String nullReplacement )
	{
		try
		{
			return StringHelper.wrap( getFunctionName( "COALESCE" ), columnName, nullReplacement );
		}
		catch ( DataSourceException e )
		{
			return StringHelper.wrap( "COALESCE", columnName, nullReplacement );
		}
	}

	protected String castNumberToVarchar( String columnName )
	{
		try
		{
			return StringHelper.wrap( getFunctionName( "CAST" ), columnName + " AS varchar(255)" );
		}
		catch ( DataSourceException e )
		{
			return StringHelper.wrap( "CAST", columnName + " AS varchar(255)" );
		}
	}

	protected String castNumberToFloat( String columnName )
	{
		try
		{
			return StringHelper.wrap( getFunctionName( "CAST" ), columnName + " AS Float" );
		}
		catch ( DataSourceException e )
		{
			return StringHelper.wrap( "CAST", columnName + " AS Float" );
		}
	}

	public String getUnicodeCharset()
	{
		String unicodeCharset;
		if ( ByteOrder.nativeOrder().equals( ByteOrder.LITTLE_ENDIAN ) )
		{
			unicodeCharset = "UTF-16LE";
		}
		else
		{
			unicodeCharset = "UTF-16BE";
		}
		return unicodeCharset;
	}

	public void disableIndexesAndConstraintsFor( String toTableName, Connection conn, String database ) throws SQLException, DataSourceException
	{
		// TODO Auto-generated method stub

	}

	public void enableIndexesAndConstraintsFor( String toTableName, Connection conn, String database ) throws SQLException, DataSourceException
	{
		// TODO Auto-generated method stub
	}

	public Boolean isValidTablespaceName( String tablespaceName ) throws DataSourceException
	{
		if ( tablespaceName != null && tablespaceName.length() > 0 )
		{
			String tablespaceQuery = getTablespaceQuery( tablespaceName );
			return doesItExist( tablespaceQuery );
		}

		return null;
	}

	public int loadViaLink( Connection conn, String fromTableName, String toTableName, String linkToLoadDB ) throws DataSourceException
	{
		return execute( conn, getInsertForLoadViaLinkQuery( fromTableName, toTableName, linkToLoadDB ) );
	}

	public int loadViaLink( Connection conn, String fromTableName, String toTableName, String fromLink, String toLink ) throws DataSourceException
	{
		return execute( conn, getInsertForLoadViaLinkQuery( fromTableName, toTableName, fromLink, toLink ) );
	}

	/*protected String getInsertForLoadViaLinkDDL(String fromTableName, String toTableName, String linkToLoadDB) throws DataSourceException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}*/

	public void createExternalTable( Table table, String dataLocation, String directory, String fileName ) throws DataSourceException
	{
		execute( getCreateExternalTableDDl( table, dataLocation, directory, fileName ) );
	}

	public void createExternalTable( Table table, String dataLocation, String directory, List<File> files ) throws DataSourceException
	{
		execute( getCreateExternalTableDDl( table, dataLocation, directory, files ) );
	}

	public void createExternalTable( Table table, String dataLocation, List<FileObjectForExtDir> files ) throws DataSourceException
	{
		execute( getCreateExternalTableDDl( table, dataLocation, files ) );
	}

	protected String getCreateExternalTableDDl( Table table, String dataLocation, List<FileObjectForExtDir> files ) throws DataSourceException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}

	protected String getCreateExternalTableDDl( Table table, String dataLocation, String directory, List<File> files ) throws DataSourceException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}

	protected String getCreateExternalTableDDl( Table table, String dataLocation, String directory, String fileName ) throws DataSourceException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}

	public void dropAllTablesMatchingName( String tableNameMatch, boolean likeStart, boolean likeEnd ) throws DataSourceException
	{
		Connection conn = null;
		Statement stmt = null;

		try
		{
			List<Object[]> tableNames = executeQuery( getAllTablesMatchingNameQuery( tableNameMatch, likeStart, likeEnd ), new ColumnDataType[]
			{ ColumnDataType.String } );
			conn = getConnection();
			stmt = conn.createStatement();

			for ( Object[] row : tableNames )
			{
				String tableName = ( String ) row[0];
				try
				{
					try
					{
						stmt.executeQuery( getTruncateTable( tableName ) );
					}
					catch ( SQLException e )
					{
					}

					try
					{
						stmt.executeQuery( getDropTableDDL( tableName, false ).get( 0 ) );
					}
					catch ( SQLException e )
					{
					}
				}
				finally
				{
					if ( conn != null )
						conn.commit();
				}

			}
		}
		catch ( SQLException e )
		{
		}
		finally
		{
			try
			{
				if ( stmt != null )
					stmt.close();
				if ( conn != null )
					conn.close();
			}
			catch ( SQLException e )
			{
			}
		}
	}

	public Boolean lockForUpdate( Connection conn, String lockForUpdateQuery ) throws DataSourceException
	{
		Statement stmt = null;
		Boolean autoCommit = null;
		try
		{
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit( false );
			stmt = conn.createStatement();

			stmt.executeUpdate( lockForUpdateQuery );
		}
		catch ( SQLException e )
		{
			try
			{
				if ( conn != null )
					conn.setAutoCommit( autoCommit );
			}
			catch ( SQLException e1 )
			{

			}
			throw new DataSourceException( "Unable to obtain lock for update for query '" + lockForUpdateQuery + "'", e );
		}
		finally
		{
			try
			{
				if ( stmt != null )
					stmt.close();
			}
			catch ( SQLException e )
			{
			}
		}
		return autoCommit;
	}

	protected ColumnDataType dbTypeToColumnDataType( String dbType, int length, Integer scale, boolean syncWithNonSpark ) throws DataSourceException
	{
		return dbTypeToColumnDataType( dbType, length );
	}

	public void loadTableForLock( String checkRowInsertedQuery, String insertQuery ) throws DataSourceException
	{
		List<Object[]> results = executeQuery( checkRowInsertedQuery, new ColumnDataType[]
		{ ColumnDataType.Int } );

		if ( results == null || results.isEmpty() )
		{
			execute( insertQuery );
		}
	}

	public String getSelectString( String tableName, String[] colNames, String whereClause )
	{
		return getSelectString( tableName, colNames );
	}

	public void lockForUpdate( Connection refConn, String tableName, String[] colNames, Object[] colValues, String whereClause ) throws DataSourceException, SQLException
	{
		throw new DataSourceException( "UnSupported Exception" );
	}

	protected abstract String getTableQuery( String database, String tablePattern, boolean exact );

	protected abstract ColumnDataType dbTypeToColumnDataType( String dbType, int length ) throws DataSourceException;

	protected abstract String columnDataTypeToDbType( ColumnDataType columnDataType, int length ) throws DataSourceException;

	protected abstract String getAllIndexDetailsQuery( String database, String tableName, boolean isViewRequired ) throws DataSourceException;

	protected abstract String getAllIndexDetailsQuery( String database, String tableName ) throws DataSourceException;

	protected abstract String getAllTableDetailsQuery( String database, String tableName, boolean isViewRequired ) throws DataSourceException;

	public abstract String getAllTableDetailsQuery( String database, String tableName ) throws DataSourceException;

	public abstract String getAllViewDetailsQuery( String database, String tableName ) throws DataSourceException;

	protected abstract List<String> getCreateTableDDL( Table table, String dataLocation ) throws DataSourceException;

	protected abstract List<String> getDropTableDDL( Table table, boolean checkExists ) throws DataSourceException;

	protected abstract List<String> getDropTableDDL( String tableName, boolean checkExists ) throws DataSourceException;

	protected abstract List<String> getCreateIndexDDL( Index index, String indexLocation ) throws DataSourceException;

	public abstract List<String> getDropIndexDDL( Index index, boolean checkExists ) throws DataSourceException;

	protected abstract List<String> getAlterTableDDL( Table oldTable, Table newTable, String dataLocation ) throws DataSourceException;

	protected abstract List<String> getUpdateTableDDL( Table oldTable, Table newTable, String dataLocation, String indexLocation ) throws DataSourceException;

	protected abstract String getAddNewColumnDDL( String fromTable, String newColumn, String dataType ) throws DataSourceException;

	public abstract String getRenameTableDDL( String fromTable, String toTable ) throws DataSourceException;

	protected abstract String getSelectDatabaseDDL( String databaseName ) throws DataSourceException;

	public abstract List<String> getTruncateTableDDL( String tableName );

	protected abstract void applyAfterTableCreateIndexes( Table table, String dataLocation, String indexLocation ) throws DataSourceException;

	public String getSqlConvert( ColumnDataType toType, int length, Column column ) throws DataSourceException
	{
		return getSqlConvert( toType, length, column.getColumnName() );
	}

	public abstract String getDefaultConstant( Column column ) throws DataSourceException;

	public abstract String getColumnRenameDDL( String tableNme, String oldColumnName, String newColumnName ) throws DataSourceException;

	public abstract String getDateTimeConversion( Object value );

	public abstract ColumnDataType externaldbTypeToColumnDataType( String dbType, int precision, int scale, boolean ignoreError ) throws DataSourceException;

	protected abstract String getTablespaceQuery( String tablespaceName );

	protected abstract String getAllTablesMatchingNameQuery( String tableNameMatch, boolean likeStart, boolean likeEnd );

	protected abstract String getInsertForLoadViaLinkQuery( String fromTableName, String toTableName, String linkToLoadDB );

	protected abstract String getInsertForLoadViaLinkQuery( String fromTableName, String toTableName, String fromLink, String toLink );
}
