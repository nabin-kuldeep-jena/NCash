package com.asjngroup.ncash.common.database.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.asjngroup.ncash.common.database.schema.ColumnDataType;
import com.asjngroup.ncash.common.database.schema.Database;
import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.database.schema.Table;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringUtil;

public class DataSourceHelper
{

	private static final Log log = LogFactory.getLog( DataSourceHelper.class );

	public static List<Object[]> unwrapResultSet( ResultSet rs, ColumnDataType[] resultTypes )
	{
		List<Object[]> results = new ArrayList<Object[]>();
		try
		{
			// build our row source wrapper
			RowSource<Object[]> rowSource = new ResultSetRowSource( rs, resultTypes );

			// loop through the result set and add each item to our return list
			while ( rowSource.next() )
			{
				results.add( rowSource.get() );
			}
		}
		finally
		{
			// close result set
			try
			{
				rs.close();
			}
			catch ( SQLException e )
			{
				log.error( "Failed to close result set", e );
			}
		}
		return results;
	}

	public static Object[] unwrapResultSetRow( ResultSet rs, ColumnDataType[] resultTypes ) throws SQLException
	{
		Object[] row = new Object[resultTypes.length];
		for ( int i = 0; i < row.length; i++ )
		{
			ColumnDataType sparkType = resultTypes[i];
			switch( sparkType )
			{
			case Long:
				row[i] = rs.getLong( i + 1 );

				// null check
				if ( rs.wasNull() )
					row[i] = null;
				break;
			case Int:
				row[i] = rs.getInt( i + 1 );

				// null check
				if ( rs.wasNull() )
					row[i] = null;
				break;
			case String:
				row[i] = rs.getString( i + 1 );
				break;
			case DateTime:
				row[i] = rs.getDate( i + 1 );
				break;
			case Bool:
				row[i] = ( rs.getString( i + 1 ) == null ? null : ( Object ) yesNoToBoolean( rs.getString( i + 1 ) ) );
				break;
			case Decimal:
				row[i] = rs.getDouble( i + 1 );
				break;
			default:
				throw new NCashRuntimeException( "Invalid spark type '%1'", sparkType );
			}
		}

		return row;
	}

	public static boolean yesNoToBoolean( String value )
	{
		// Turn into the boolean value
		return value.equals( "Y" );
	}

	public static void validateResultSet( ResultSet rs, ColumnDataType[] resultTypes ) throws SQLException
	{
		int columnCount = rs.getMetaData().getColumnCount();

		// make sure we have the right metadata about the results
		if ( resultTypes.length != columnCount )
			throw new SQLException( "The query returned " + columnCount + " columns when " + resultTypes.length + " was expected" );

		// check that the types we are expecting are compatible with the result columns
		for ( int i = 0; i < resultTypes.length; i++ )
		{
			// check differently for each spark type
			ColumnDataType sparkType = resultTypes[i];
			int columnType = rs.getMetaData().getColumnType( i + 1 );
			String columnTypeName = rs.getMetaData().getColumnTypeName( i + 1 );
			String columnName = rs.getMetaData().getColumnName( i + 1 );
			switch( sparkType )
			{
			case Long:
				if ( columnType != Types.BIGINT && columnType != Types.INTEGER && columnType != Types.NUMERIC && columnType != Types.LONGVARCHAR )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			case Int:
				if ( columnType != Types.INTEGER && columnType != Types.NUMERIC && columnType != Types.BIGINT )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			case String:
				if ( columnType != Types.VARCHAR && columnType != Types.CHAR && columnType != Types.NVARCHAR )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			case DateTime:
				if ( columnType != Types.TIMESTAMP && columnType != Types.DATE )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			case Bool:
				if ( columnType != Types.CHAR && columnType != Types.VARCHAR )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			case Decimal:
				if ( columnType != Types.BIGINT && columnType != Types.NUMERIC && columnType != Types.DOUBLE )
					throw new SQLException( "For table column " + columnName + " expecting data type " + columnTypeName + " but found " + sparkType + " as output data type ." );
				break;
			default:
				log.error( "Invalid type in configured output that is :" + sparkType );
				throw new SQLException( "Invalid spark type passed " + sparkType );
			}
		}
	}

	public static int executeUpdate( Connection connection, String sqlTemplate ) throws DataSourceException
	{
		return executeUpdate( connection, sqlTemplate, new String[0], new Object[0] );
	}

	public static int executeUpdate( Connection connection, String sqlTemplate, String strings, Object paramValue ) throws DataSourceException
	{
		return executeUpdate( connection, sqlTemplate, new String[]
		{ strings }, new Object[]
		{ paramValue } );
	}

	public static int executeUpdate( Connection connection, String sqlTemplate, String[] paramNames, Object[] paramValues ) throws DataSourceException
	{
		PreparedStatement statement = null;
		int rows = -1;
		try
		{
			// Replace named parameters with jdbc placeholders
			Map<String, Integer> paramIdxs = new HashMap<String, Integer>();
			String sql = replaceNamedParameters( sqlTemplate, paramNames, paramIdxs );

			// Build a prepared statement for the query
			statement = connection.prepareStatement( sql );

			// Set the parameters
			for ( int i = 0; i < paramNames.length; i++ )
				DataSourceHelper.setPreparedStatementParameter( statement, paramIdxs.get( paramNames[i] ), paramValues[i] );

			// Execute the update prepared statement
			rows = statement.executeUpdate();
			connection.commit();
		}
		catch ( SQLException e )
		{
			throw new DataSourceException( e );
		}
		finally
		{
			// Cleanup the statement
			if ( statement != null )
			{
				try
				{
					statement.close();
				}
				catch ( SQLException e )
				{
					log.error( "Failed to close the statement", e );
				}
			}

			if ( connection != null )
				try
				{
					connection.close();
				}
				catch ( SQLException e )
				{
					log.error( "Failed to close the connection", e );
				}
		}

		// Return the rows affected
		return rows;
	}

	public static void setPreparedStatementParameter( PreparedStatement statement, int paramIndex, Object paramValue, ColumnDataType paramType )
	{
		// sanity check
		if ( paramValue == null && paramType == null )
			throw new NCashRuntimeException( "Parameter index %1 must have a type specified since the value is null.", paramIndex );

		try
		{
			// if the parameter is null set the null value
			if ( paramValue == null )
			{
				statement.setNull( paramIndex, paramType.toJDBCType() );
				return;
			}

			// derive the type if it is null
			if ( paramType == null )
				paramType = ColumnDataType.typeof( paramValue );

			// set relevant parameter type
			switch( paramType )
			{
			case Long:
				statement.setLong( paramIndex, ( Long ) paramValue );
				break;

			case Int:
				statement.setInt( paramIndex, ( Integer ) paramValue );
				break;

			case String:
				statement.setString( paramIndex, ( String ) paramValue );
				break;

			case DateTime:
				statement.setDate( paramIndex, ( Date ) paramValue );
				;
				break;

			case Bool:
				statement.setBoolean( paramIndex, ( Boolean ) paramValue );
				break;

			case Decimal:
				statement.setLong( paramIndex, ( Long ) paramValue );
				break;

			default:
				throw new IllegalArgumentException( StringUtil.create( "Invalid spark type '%1' to bind to parameters", paramType ) );
			}
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( "Error setting parameter index %1 with value '%2' and type '%3'", paramIndex, paramValue, paramType );
		}
		catch ( HibernateException e )
		{
			throw new NCashRuntimeException( "Error setting parameter index %1 with value '%2' and type '%3'", paramIndex, paramValue, paramType );
		}
	}

	public static void setPreparedStatementParameter( PreparedStatement statement, int paramIndex, Object paramValue ) throws SQLException
	{
		try
		{

			// Don't handle null values
			if ( paramValue == null )
			{
				statement.setObject( paramIndex, null );
			}
			// Detect the parameter type and call the appropriate function to set the value
			else if ( paramValue instanceof Integer )
			{
				statement.setInt( paramIndex, ( ( Integer ) paramValue ) );
			}
			else if ( paramValue instanceof Long )
			{
				statement.setLong( paramIndex, ( ( Long ) paramValue ) );
			}
			else if ( paramValue instanceof String )
			{
				statement.setString( paramIndex, ( ( String ) paramValue ) );
			}
			else if ( paramValue instanceof DateTime )
			{
				statement.setDate( paramIndex, ( Date ) paramValue );
			}
			else if ( paramValue instanceof Boolean )
			{
				// Turn into a yes/no flag
				statement.setString( paramIndex, booleanToYesNo( ( ( Boolean ) paramValue ).booleanValue() ) );
			}
			else if ( paramValue instanceof BigDecimal )
			{
				statement.setLong( paramIndex, ( Long ) paramValue );
			}
			else
			{
				throw new IllegalArgumentException( StringUtil.create( "Unknown paramValue type '%1'.", paramValue.getClass() ) );
			}
		}
		catch ( HibernateException e )
		{
			throw new SQLException( "Hibernate exception: " + e.getMessage() );
		}
	}

	public static String booleanToYesNo( boolean value )
	{
		// Turn into the flag value
		return ( value ? "Y" : "N" );
	}

	public static String replaceNamedParameters( String sqlTemplate, String[] paramNames, Map<String, Integer> paramIdxs ) throws DataSourceException
	{
		// Loop through replacing the parameters with jdbc '?' placeholders
		// We have to keep track of the jdbc ordinal parameter value whenever we replace a parameter
		// so that we can later call functions on the prepared statements which are parameter index based
		String sql = sqlTemplate;
		paramIdxs.clear();

		// create a sorted list for the parameters names, which will be in order of decreasing length
		// NOTE: we must replace the longest parameters first as otherwise 2 parameters such as
		//       :arg1 and :arg11 will create incorrect SQL
		SortedMap<String, Integer> sortedParamNames = new TreeMap<String, Integer>( new NamedParameterComparator() );

		// add the parameter names to the sorted map first (with null as a map value) so they are ordered
		for ( int i = 0; i < paramNames.length; i++ )
			sortedParamNames.put( paramNames[i], null );

		Iterator iterator = sortedParamNames.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			// get the map entry
			Map.Entry<String, Integer> mapEntry = ( Map.Entry<String, Integer> ) iterator.next();

			// Keep track of the position of the parameter (making sure it exists)
			String paramName = ":" + mapEntry.getKey();
			if ( sql.contains( paramName ) )
				mapEntry.setValue( sql.indexOf( paramName ) );
			else
				throw new DataSourceException( "Parameter '" + mapEntry.getKey() + "' does not exist in the query text '" + sqlTemplate + "'" );
		}

		// create a map to have the parameter names in order of appearance
		Map<Integer, String> stringIdxs = new TreeMap<Integer, String>();

		// get the iterator again and replace all the parameter names with the placeholder character
		// NOTE: we must do this is a seperate loop from the above iteration as otherwise the sql string would change
		//       and the index of the param name would change
		iterator = sortedParamNames.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			// get the map entry
			Map.Entry<String, Integer> mapEntry = ( Map.Entry<String, Integer> ) iterator.next();

			// Replace the parameter and add to the param index map
			String paramName = mapEntry.getKey();
			sql = sql.replace( ":" + paramName, "?" );
			stringIdxs.put( mapEntry.getValue(), mapEntry.getKey() );
		}

		// now populate the parameter index list
		int idx = 1;
		iterator = stringIdxs.values().iterator();
		while ( iterator.hasNext() )
		{
			String paramName = ( String ) iterator.next();
			paramIdxs.put( paramName, idx );
			idx++;
		}

		// finally, make sure there are no parameters left
		for ( int i = 0; i < paramNames.length; i++ )
		{
			// Check for the parameter
			String paramName = ":" + paramNames[i];
			if ( sql.contains( paramName ) )
				throw new DataSourceException( "Parameter '" + paramNames[i] + "' may exist more than once in the query text '" + sqlTemplate + "'" );
		}

		// Return the placeholdered query
		return sql;
	}

	public static void syncDatabaseToSchema( Schema schema, DataSource dataSource, boolean fullUpdate, boolean allowIndexUpdate, List<String> allowCreateTableNames, List<String> allowUpdateTableNames ) throws DataSourceException
	{
		// create final variables for our inner class
		final boolean fullUpdateF = fullUpdate;
		final boolean allowIndexUpdateF = allowIndexUpdate;
		final List<String> allowCreateTableNamesF = new ArrayList<String>();
		final List<String> allowUpdateTableNamesF = new ArrayList<String>();

		// lower case all the table names
		for ( String tableName : allowCreateTableNames )
			allowCreateTableNamesF.add( tableName.toLowerCase() );

		for ( String tableName : allowUpdateTableNames )
			allowUpdateTableNamesF.add( tableName.toLowerCase() );

		SyncDatabaseListener syncDBListener = new SyncDatabaseListener()
		{
			public boolean createTable( Table table )
			{
				// create if this is a full update or our create list contains this table
				if ( fullUpdateF || allowCreateTableNamesF.contains( table.TableName.toLowerCase() ) )
					return true;
				else
					return false;
			}

			public boolean updateTable( Table fromTable, Table toTable )
			{
				// update if this is a full update or our update list contains this table
				if ( fullUpdateF || allowUpdateTableNamesF.contains( toTable.TableName.toLowerCase() ) )
					return true;
				else
					return false;
			}

			public boolean updateIndex( Index fromIndex, Index toIndex )
			{
				// always allow index drops
				if ( toIndex == null )
					return true;

				// update the index this is a full update or updating just the indexes
				return ( fullUpdateF || allowIndexUpdateF );
			}

			public boolean dropTable( Table table )
			{
				// never allow dropping of tables here
				return false;
			}
		};

		int totalTables = allowCreateTableNamesF.size() + allowUpdateTableNamesF.size();
		if ( totalTables == 1 )
		{
			// sync the database to the reference schema for only one table
			String tableName = allowCreateTableNamesF.size() > 0 ? allowCreateTableNamesF.get( 0 ) : allowUpdateTableNamesF.get( 0 );
			DataSourceHelper.syncDatabaseToSchema( schema, dataSource, "", "", syncDBListener, tableName );
		}
		else
		{
			// sync the database to the reference schema
			DataSourceHelper.syncDatabaseToSchema( schema, dataSource, "", "", syncDBListener );
		}
	}

	public static void syncDatabaseToSchema( Schema sourceSchema, DataSource dataSource, String dataLocation, String indexLocation, SyncDatabaseListener listener ) throws DataSourceException
	{
		DataSourceHelper.syncDatabaseToSchema( sourceSchema, dataSource, dataLocation, indexLocation, listener, null );
	}

	public static void syncDatabaseToSchema( Schema sourceSchema, DataSource dataSource, String dataLocation, String indexLocation, SyncDatabaseListener listener, String tableName ) throws DataSourceException
	{
		// get the current database schema
		Schema destinationSchema = null;

		if ( tableName != null )
		{
			long start_time = System.currentTimeMillis();
			destinationSchema = dataSource.getSchema( dataSource.getDatabase(), tableName );
			log.info( "Single table sync time in milli's: " + ( System.currentTimeMillis() - start_time ) );
		}
		else
		{
			long start_time = System.currentTimeMillis();
			destinationSchema = dataSource.getSchema( dataSource.getDatabase() );
			log.info( "Database sync time in milli's: " + ( System.currentTimeMillis() - start_time ) );
		}

		// patch the source schema
		Schema patchedSourceSchema = dataSource.preCompareSchemaPatch( sourceSchema );

		Database destinationDatabase = destinationSchema.findDatabase( dataSource.getDatabase() );
		Database sourceDatabase = patchedSourceSchema.Databases.get( 0 );

		if ( sourceDatabase == null )
		{
			throw new DataSourceException( "Source database not found in source schema" );
		}

		// destination db doesn't exist
		if ( destinationDatabase == null )
		{
			throw new DataSourceException( "Destination database has not been created: %1", dataSource.getDatabase() );
		}

		List<Table> createTables = new ArrayList<Table>();
		List<Table[]> updateTables = new ArrayList<Table[]>();
		List<Index[]> changedIndexes = new ArrayList<Index[]>();

		log.info( "Number of tables in source schema: " + sourceDatabase.Tables.size() );
		// scan for tables that need adding or updating
		for ( Table table : sourceDatabase.Tables )
		{
			Table destinationTable = destinationDatabase.findTable( table.TableName );
			if ( destinationTable == null )
			{
				createTables.add( table );
				continue;
			}

			// if the source table is not structurally equivalent to the destination table, update it
			if ( !table.isTableStructureEqual( destinationTable ) )
			{
				// going from the "destination" ie current table to the source schema table
				updateTables.add( new Table[]
				{ destinationTable, table } );

				// since the table is recreated, we need to recreate the indexes
				for ( Index index : table.Indexes )
					changedIndexes.add( new Index[]
					{ null, index } );
			}
			else if ( !table.areIndexesEqual( destinationTable ) )
			{
				// loop over the schema defined indexes
				for ( Index index : table.Indexes )
				{
					Index destinationIndex = destinationTable.findIndex( index.IndexName );
					if ( destinationIndex == null )
					{
						// the will need to be created
						changedIndexes.add( new Index[]
						{ null, index } );
					}
					else
					{
						// see if the index needs to be recreated
						if ( !index.equals( destinationIndex ) )
							changedIndexes.add( new Index[]
							{ destinationIndex, index } );
					}
				}

				// look for indexes that will need to be dropped from the existing table
				for ( Index destinationIndex : destinationTable.Indexes )
				{
					Index index = table.findIndex( destinationIndex.IndexName );
					if ( index == null )
						changedIndexes.add( new Index[]
						{ destinationIndex, null } );
				}
			}
		}

		for ( Table table : createTables )
		{
			if ( listener == null || listener.createTable( table ) )
			{
				log.debug( "Creating table " + table.TableName );
				dataSource.createTable( table, dataLocation, indexLocation );
			}
		}

		for ( Table[] table : updateTables )
		{
			if ( listener == null || listener.updateTable( table[0], table[1] ) )
			{
				log.debug( "Updating table " + table[0].TableName );
				dataSource.updateTable( table[0], table[1], dataLocation );
			}
		}

		List<Index[]> dropIndexes = new ArrayList<Index[]>();
		List<Index[]> createIndexes = new ArrayList<Index[]>();
		List<Index[]> updateIndexes = new ArrayList<Index[]>();

		for ( Index[] index : changedIndexes )
		{
			if ( index[0] == null )
				createIndexes.add( index );
			else if ( index[1] == null )
				dropIndexes.add( index );
			else
				updateIndexes.add( index );
		}

		for ( Index[] index : dropIndexes )
		{
			if ( listener == null || listener.updateIndex( index[0], index[1] ) )
			{
				// display the appropiate debug message
				log.debug( "Dropping index " + index[0].IndexName );
				dataSource.updateIndex( index[0], index[1], indexLocation );
			}
		}

		for ( Index[] index : updateIndexes )
		{
			if ( listener == null || listener.updateIndex( index[0], index[1] ) )
			{
				// display the appropiate debug message
				log.debug( "Updating index " + index[0].IndexName );
				dataSource.updateIndex( index[0], index[1], indexLocation );
			}
		}

		for ( Index[] index : createIndexes )
		{
			if ( listener == null || listener.updateIndex( index[0], index[1] ) )
			{
				// display the appropiate debug message
				log.debug( "Creating index " + index[1].IndexName );
				dataSource.updateIndex( index[0], index[1], indexLocation );
			}
		}
	}

}
