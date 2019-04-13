package com.asjngroup.ncash.common.database.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import com.asjngroup.ncash.common.NCashDateFormatter;
import com.asjngroup.ncash.common.database.helper.IdGenerator;
import com.asjngroup.ncash.common.database.helper.MysqlIdGenerator;
import com.asjngroup.ncash.common.database.schema.Column;
import com.asjngroup.ncash.common.database.schema.ColumnDataType;
import com.asjngroup.ncash.common.database.schema.Database;
import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.database.schema.Table;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.io.util.FileHelper;
import com.asjngroup.ncash.common.util.StringHelper;

public class MySqlDataSource extends AbstractDataSource
{
	private static final Log log = LogFactory.getLog( AbstractDataSource.class );
	private MysqlIdGenerator idGenerator = new MysqlIdGenerator();
	private static Map<Integer, String> dayOfWeekGroupMap = new HashMap<Integer, String>();
	private static Map<String, String> functionNameMap = new HashMap<String, String>();
	private static String hint;
	private static String degree;

	static
	{
		dayOfWeekGroupMap.put( DateTimeConstants.MONDAY, "19000101" );
		dayOfWeekGroupMap.put( DateTimeConstants.TUESDAY, "19000102" );
		dayOfWeekGroupMap.put( DateTimeConstants.WEDNESDAY, "19000103" );
		dayOfWeekGroupMap.put( DateTimeConstants.THURSDAY, "19000104" );
		dayOfWeekGroupMap.put( DateTimeConstants.FRIDAY, "19000105" );
		dayOfWeekGroupMap.put( DateTimeConstants.SATURDAY, "19000106" );
		dayOfWeekGroupMap.put( DateTimeConstants.SUNDAY, "19000107" );

		functionNameMap.put( "ISNULL", "NVL" );
		functionNameMap.put( "SUBSTRING", "SUBSTR" );
		functionNameMap.put( "CONVERT", "TO_CHAR" );
		functionNameMap.put( "BINARY_CHECKSUM", "ORA_HASH" );
		functionNameMap.put( "CAST", "CAST" );
	}

	public MySqlDataSource( DataSource dataSource ) throws SQLException
	{
		super( dataSource );
	}

	public void decorateResultSet( ResultSet rs )
	{
		try
		{
			rs.setFetchSize( 1000 );
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e.getCause() );
		}

	}

	public DatabaseType getDatabaseType()
	{
		return DatabaseType.MySql;
	}

	public void closeAll()
	{
		// TODO Auto-generated method stub

	}

	public DateTimeFormatter getBcpDateFormatter()
	{
		return DateTimeFormat.forPattern( "yyyyMMdd" );
	}

	public DateTimeFormatter getBcpDateTimeFormatter()
	{
		return DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss" );
	}

	public DateTimeFormatter getBcpMilliDateTimeFormatter()
	{
		return DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss.SSSSSS" );
	}

	public String getSqlDateConstant( DateTime dateTime, DateTimeFormatter dateTimeFormatter )
	{
		return "TO_TIMESTAMP('" + dateTimeFormatter.print( dateTime ) + "', 'YYYYMMDD HH24:MI:SS')";
	}

	public Charset getBcpCharset()
	{
		return ( isUnicode || FileHelper.isUnicodeFileEncoding ? FileHelper.unicodeCharset : FileHelper.platformCharset );
	}

	public String getUniqueConnectionId( Connection connection ) throws DataSourceException
	{
		List<Object[]> results = executeQuery( connection, "select userenv( 'sessionid' ) from dual", new ColumnDataType[]
		{ ColumnDataType.Long } );

		if ( results.size() != 1 )
		{
			throw new DataSourceException( "Got '%1' rows querying for session id, expected 1", results.size() );
		}

		Long sid = ( Long ) results.get( 0 )[0];

		return sid.toString() + "_" + getValue();
	}

	private Integer getValue()
	{
		Integer val = ( new Random() ).nextInt( 1000 );
		if ( val < 0 )
		{
			return -val;
		}
		return val;
	}

	public String getDifferenceOperator()
	{
		return "minus";
	}

	public void truncateTable( String database, String tableName ) throws DataSourceException
	{
		truncateTable( database, tableName, false );
	}

	public void truncateTable( String database, String tableName, boolean executeWithWait ) throws DataSourceException
	{
		String query = "truncate table " + tableName;
		if ( executeWithWait )
		{
			Connection conn = null;
			try
			{
				conn = getConnection();
				executeDDLQueryWithWait( query, conn );
			}
			finally
			{
				try
				{
					if ( conn != null )
						conn.close();
				}
				catch ( SQLException e )
				{
					log.error( e );
				}
			}
		}
		else
		{
			execute( query );
		}
	}

	public void dropStoredProcedure( String procName ) throws DataSourceException
	{
		// TODO Auto-generated method stub

	}

	public void applyStoredProcedure( Map<String, String> map, String procName, String macro ) throws DataSourceException
	{
		// TODO Auto-generated method stub

	}

	public List<String[]> executeStoredProcedure( String procName, List args ) throws DataSourceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getDataSourceName()
	{
		// TODO Auto-generated method stub
		return "MySql";
	}

	public IdGenerator getIdGenerator()
	{
		return idGenerator;
	}

	public String getSqlToFetchNRows( String tableName, long maxRows )
	{
		return "select * from " + tableName + " where rownum <= " + maxRows;
	}

	public Schema preCompareSchemaPatch( Schema schema ) throws DataSourceException
	{
		Schema patchSchema = null;
		try
		{
			patchSchema = ( Schema ) schema.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new DataSourceException( e );
		}

		// make all indexes non-clustered.
		for ( Database database : patchSchema.Databases )
		{
			for ( Table table : database.Tables )
			{
				for ( Index index : table.Indexes )
				{
					if ( index.IsClustered )
					{
						index.IsClustered = false;
					}
				}
			}
		}

		return patchSchema;
	}

	public String escapeString( String s )
	{
		return s;
	}

	public String getOrderByClause( List<Object[]> colNames, ColumnDataType[] resultTypes ) throws Exception
	{
		List<String> columnNames = new ArrayList<String>();
		int i = 0;
		for ( Object[] column : colNames )
		{
			if ( !ColumnDataType.Unknown.equals( resultTypes[i++] ) )
				columnNames.add( column[0].toString() );
		}
		return StringHelper.merge( columnNames, "," );
	}

	public ColumnDataType externalDBTypeToColumnDataType( String dbType, int precision, int scale, boolean ignoreError ) throws DataSourceException
	{

		if ( dbType.toUpperCase().startsWith( "TIMESTAMP" ) || dbType.equalsIgnoreCase( "date" ) )
			return ColumnDataType.DateTime;
		if ( dbType.equalsIgnoreCase( "float" ) || ( dbType.equalsIgnoreCase( "number" ) && scale != 0 ) )
			return ColumnDataType.Decimal;
		if ( dbType.equalsIgnoreCase( "number" ) && precision <= 10 )
			return ColumnDataType.Int;
		if ( dbType.equalsIgnoreCase( "number" ) && precision <= 19 )
			return ColumnDataType.Long;
		if ( dbType.equalsIgnoreCase( "nvarchar2" ) )
			return ColumnDataType.String;
		if ( dbType.equalsIgnoreCase( "varchar2" ) || dbType.equalsIgnoreCase( "varchar" ) || dbType.equalsIgnoreCase( "char" ) || dbType.equalsIgnoreCase( "nchar" ) )
			return ColumnDataType.String;
		if ( ignoreError )
			return ColumnDataType.Unknown;
		else
			throw new DataSourceException( "DB Type '" + dbType + "' not supported" );
	}

	public String convertDateTimeToDBFormat( Column column )
	{
		StringBuffer resultValue = new StringBuffer();
		String dateValue;
		if ( ColumnDataType.DateTime.equals( column.getDataType() ) )
		{
			DateTime dateTime = ( DateTime ) column.getValue();
			dateValue = dateTime.toString( NCashDateFormatter.dateFormatStringForStorage );

			resultValue.append( "to_date( '" ).append( dateValue ).append( "','" ).append( NCashDateFormatter.dateFormatStringForMySQlStorage ).append( "')" );

			return resultValue.toString();
		}
		return null;
	}

	public String getFunctionName( String functionName ) throws DataSourceException
	{
		String newFunctionName = functionNameMap.get( functionName.toUpperCase() );

		if ( newFunctionName == null )
		{
			throw new DataSourceException( "Unrecognised function %1", functionName );
		}

		return newFunctionName;
	}

	public boolean doesColumnExist( String tableName, String columnName ) throws DataSourceException
	{
		try
		{
			String sqlQuery = "SELECT * FROM all_tab_cols where table_name ='" + tableName.toUpperCase() + "' and column_name = '" + columnName.toUpperCase() + "' and owner = '" + getUserName().toUpperCase() + "'";
			return doesItExist( sqlQuery );
		}
		catch ( Exception e )
		{
			throw new DataSourceException( e );
		}
	}

	public String getConcatOperator()
	{
		return " || ";
	}

	public String getHybridDataCopyQuery( String fromTableName, String toTableName )
	{
		return "INSERT /*+ APPEND */ INTO " + toTableName + " SELECT /*+ PARALLEL(" + fromTableName + ",4) */ * FROM " + fromTableName;
	}

	public String getDropIndexDDL( String tableName, String indexName, boolean checkExists ) throws DataSourceException
	{
		try
		{
			String statement;

			// Generate the drop index
			statement = "";

			if ( checkExists )
			{
				// Check for existence
				statement = "declare v_count number; " + "begin " + "  select count(1) into v_count from all_indexes" + "  where index_name = '" + indexName.toUpperCase() + "'" + "  and owner = '" + getConnection().getMetaData().getUserName().toUpperCase() + "'" + "  and table_name = '" + tableName.toUpperCase() + "';" + "  if v_count > 0 then" + "    execute immediate 'drop index " + indexName + "';" + "  end if;" + "end;";
			}
			else
			{
				// Just drop the index
				statement = "DROP INDEX " + indexName + " ON " + tableName;
			}

			// Return the DDL
			return statement;
		}
		catch ( Exception e )
		{
			throw new DataSourceException( e );
		}
	}

	public List<String> getCreateIndexDDL( Index index, String indexLocation ) throws DataSourceException
	{
		Boolean isValidTablespace = isValidTablespaceName( indexLocation );
		if ( isValidTablespace != null && !isValidTablespace )
			throw new DataSourceException( "Invalid Index Location: Invalid Tablespace name '" + indexLocation + "' specified." );

		List<String> sqlList = new ArrayList<String>();

		// Get the SQL to add this regular index
		sqlList.add( getCreateIndexStatement( index, indexLocation ) );

		// Return the DDL
		return sqlList;
	}

	private String getCreateIndexStatement( Index index, String indexLocation ) throws DataSourceException
	{
		try
		{
			String statement = "declare v_count number;" + " begin" + " select  count(1) into v_count " + " from    INFORMATION_SCHEMA.STATISTICS" + " where   INDEX_NAME  = '" + index.IndexName.toUpperCase() + "'" + " and     INDEX_SCHEMA       = '" + getDatabase() + "'" + " and     TABLE_SCHEMA       = '" + getDatabase() + "'" + " and     table_name  = '" + index.table.TableName.toUpperCase() + "';" + " if v_count = 0 then" + "    execute immediate '" + "        create" + ( index.IsUnique ? " unique" : "" ) + " index " + index.IndexName + "        on " + index.table.TableName + "(" + index.ColumnList + ") ";

			// Add the storage options if specified
			if ( indexLocation != null && indexLocation.length() > 0 )
				statement += " tablespace " + indexLocation + " ";

			// Add the last part
			statement += " nologging ";
			if ( this.hint != null && this.degree != null )
			{
				statement += " " + hint + " " + degree;
			}

			statement += "    ';" + "  end if;" + " end;";

			statement = "        create" + ( index.IsUnique ? " unique" : "" ) + " index " + index.IndexName + "        on " + index.table.TableName + "(" + index.ColumnList + ") ";
			return statement;
		}
		catch ( Exception e )
		{
			throw new DataSourceException( e );
		}

	}

	public String getRenameTableDDL( String fromTable, String toTable, boolean checkExists ) throws DataSourceException
	{
		try
		{
			String statement;

			// Generate the drop table
			if ( checkExists )
			{
				// Check for existence
				statement = "declare v_count number;" + " begin" + " select count(1) into v_count from all_tables" + " where table_name = '" + fromTable.toUpperCase() + "' and owner = '" + getConnection().getMetaData().getUserName().toUpperCase() + "';" + " if v_count > 0 then" + "    execute immediate 'rename " + fromTable + " to " + toTable + "';" + "  end if;" + " end;";
			}
			else
			{
				// Just drop the table
				statement = "rename " + fromTable + " to " + toTable;
			}

			// Return the DDL
			return statement;
		}
		catch ( Exception e )
		{
			throw new DataSourceException( e );
		}

	}

	public List<String> getTruncateTableDDL( String tableName )
	{
		List<String> sqlList = new ArrayList<String>();
		String query = "truncate table " + tableName;

		sqlList.add( query );
		return sqlList;
	}

	public String getCreateIndexDDL( String tinTableName, String indexName, boolean uniqueFl, String columnList, String indexLocation ) throws DataSourceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getDropTable( String tableName, boolean checkExists ) throws DataSourceException
	{
		try
		{
			List<String> sqlList = new ArrayList<String>();
			String statement;

			// Generate the drop table
			if ( checkExists )
			{
				// Check for existence
				statement = "declare v_count number;" + " begin" + " select count(1) into v_count from all_tables" + " where table_name = '" + tableName.toUpperCase() + "' and owner = '" + getConnection().getMetaData().getUserName().toUpperCase() + "';" + " if v_count > 0 then" + "    execute immediate 'drop table " + tableName + "';" + "  end if;" + " end;";
			}
			else
			{
				// Just drop the table
				statement = "drop table " + tableName;
			}
			sqlList.add( statement );

			// Return the DDL
			return sqlList;
		}
		catch (

		Exception e )
		{
			throw new DataSourceException( e );
		}
	}

	public List<String> getDropTable( Table table, boolean checkExists ) throws DataSourceException
	{
		return getDropTableDDL( table.TableName, checkExists );
	}

	public String getTruncateTable( String tableName ) throws DataSourceException
	{
		return "truncate table " + tableName;
	}

	public void dropColumnNullConstraint( Connection connection, String tableName, String colName, String optDataType ) throws DataSourceException
	{
		String queryString = "";
		queryString += "ALTER TABLE " + tableName + "  MODIFY  " + colName + " NULL";
		DataSourceHelper.executeUpdate( connection, queryString );

	}

	public String getSqlConvert( ColumnDataType toType, int length, String columnName ) throws DataSourceException
	{
		// make sql-server DataSource do cast as well...
		try
		{
			return " " + getFunctionName( "cast" ) + "( " + columnName + " as " + this.columnDataTypeToDbType( toType, length ) + " ) ";
		}
		catch ( DataSourceException e )
		{
			throw e;
		}
	}

	public String getTrimFunction( String columnName )
	{
		return "trim( '" + columnName + "' )";
	}

	public String convertDateTimeToDate( String columnWithPrefix ) throws DataSourceException
	{
		StringBuilder dateString = new StringBuilder();
		dateString.append( "to_date(to_char(" );
		dateString.append( columnWithPrefix );
		dateString.append( " , 'dd-mm-yyyy'), 'dd-mm-yyyy' )" );
		return dateString.toString();
	}

	public String convertDateTimeToDateWithHour( String columnWithPrefix ) throws DataSourceException
	{
		StringBuilder query = new StringBuilder();
		query.append( "to_date(to_char( " ).append( columnWithPrefix ).append( " ,'dd-mm-yyyy HH24'), 'dd-mm-yyyy HH24')" );
		return query.toString();
	}

	public String getHashFunctionQuery( String hashTableColumnName, Integer totolThreadCount, Integer threadNumber )
	{
		return null;
	}

	protected String getTableQuery( String database, String tableName, boolean exact )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( " select  tab.TABLE_NAME " );
		sb.append( " from      all_tables         tab " );
		sb.append( " where     tab.OWNER             = '" + database.toUpperCase() + "' " );

		if ( tableName != null && tableName.length() > 0 )
		{
			if ( exact )
			{
				sb.append( " and     tab.TABLE_NAME       = '" + tableName.toUpperCase() + "' " );
			}
			else
			{
				sb.append( " and     tab.TABLE_NAME       like '" + tableName.toUpperCase() + "' " );
			}
		}

		sb.append( " order by tab.TABLE_NAME" );

		return sb.toString();
	}

	protected ColumnDataType dbTypeToColumnDataType( String dbType, int length ) throws DataSourceException
	{
		return dbTypeToColumnDataType( dbType, length, null, false );
	}

	protected ColumnDataType dbTypeToColumnDataType( String dbType, int length, Integer scale, boolean syncWithNonNcash ) throws DataSourceException
	{
		boolean hasPrecision = scale != null && scale > 0;

		if ( dbType.equalsIgnoreCase( "char" ) )
			return ColumnDataType.Bool;
		if ( dbType.toUpperCase().startsWith( "TIMESTAMP" ) || dbType.toUpperCase().startsWith( "DATE" ) )
			return ColumnDataType.DateTime;
		if ( ( dbType.equalsIgnoreCase( "numeric" ) || dbType.equalsIgnoreCase( "decimal" ) ) && hasPrecision )
			return ColumnDataType.Decimal;
		if ( ( dbType.equalsIgnoreCase( "number" ) || dbType.equalsIgnoreCase( "int" ) ) && length <= 10 )
			return ColumnDataType.Int;
		if ( ( dbType.equalsIgnoreCase( "number" ) || dbType.equalsIgnoreCase( "int" ) ) && ( length <= 19 || ( length >= 20 && syncWithNonNcash ) ) )
			return ColumnDataType.Long;
		if ( dbType.equalsIgnoreCase( "nvarchar2" ) )
			return ColumnDataType.String;
		if ( dbType.equalsIgnoreCase( "varchar2" ) || dbType.equalsIgnoreCase( "varchar" ) )
			return ColumnDataType.String;
		if ( dbType.equalsIgnoreCase( "nclob" ) )
			throw new DataSourceException( " nclob for oracle not supported " );//return ncashType.Text;

		return null;
	}

	@Override
	protected String columnDataTypeToDbType( ColumnDataType columnDataType, int length ) throws DataSourceException
	{
		switch( columnDataType )
		{
		case Bool:
			return "char";
		case DateTime:
			return "timestamp";
		case Int:
			return "INT";
		case Long:
		case Decimal:
			return "decimal";
		case String:
			return ( isUnicode ? "varchar(" + length + ")" : "varchar(" + length + ")" );
		case Text:
			throw new DataSourceException( " NCashType: Text is not supported for MYSQL" );
		default:
			throw new DataSourceException( "Unknown '%1': '%2'", "ColumnDataType", columnDataType );
		}
	}

	protected String getAllIndexDetailsQuery( String database, String tableName )
	{
		return getAllIndexDetailsQuery( database, tableName, false );
	}

	public String getAllIndexDetailsQuery( String database, String tableName, boolean isViewRequired )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "select  inx.TABLE_NAME as table_name,                       " );
		sb.append( "        inx.index_name as index_name,                       " );
		sb.append( "        inx.column_name,                                        " );
		sb.append( "        case                                                " );
		sb.append( "            when inx.index_type = 'IOT - TOP' then 'Y'      " );
		sb.append( "            else 'N'                                        " );
		sb.append( "        end,                                                " );
		sb.append( "        case                                                " );
		sb.append( "            when inx.NON_UNIQUE = 0 then 'Y'         " );
		sb.append( "            when inx.NON_UNIQUE = 1 then 'N'      " );
		sb.append( "        end,                                                " );
		sb.append( "        'N' as ignore_dup_key,                              " );
		sb.append( "        inc.ORDINAL_POSITION " );
		sb.append( "from    INFORMATION_SCHEMA.STATISTICS  inx, " );
		sb.append( "   INFORMATION_SCHEMA.COLUMNS  inc " );
		sb.append( "where   inx.INDEX_SCHEMA           = '" + database + "'        " );
		sb.append( "and   inx.TABLE_SCHEMA           = '" + database + "'        " );
		sb.append( "and     inx.table_name      = inc.table_name                " );
		sb.append( "and     inx.TABLE_SCHEMA      = inc.TABLE_SCHEMA                " );

		// MED: Add this back in for Oracle 10g but it won't work on 9i
		//        sb.append("and     inx.dropped         = 'NO' " );

		if ( tableName != null && tableName.length() > 0 )
			sb.append( " and     inx.table_name       = '" + tableName.toUpperCase() + "' " );

		sb.append( "order by inx.table_name, inx.index_name,inc.ORDINAL_POSITION " );

		return sb.toString();
	}

	protected String getAllTableDetailsQuery( String database, String tableName, boolean isViewRequired )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( " select   tab.TABLE_NAME, " );
		sb.append( "          col.COLUMN_NAME, " );
		sb.append( "          col.DATA_TYPE, " );
		sb.append( "              case " );
		sb.append( "                  when col.CHARACTER_MAXIMUM_LENGTH is NULL then col.NUMERIC_PRECISION " );
		sb.append( "                  else col.CHARACTER_MAXIMUM_LENGTH " );
		sb.append( "              end ," );
		sb.append( "          cast( " );
		sb.append( "              col.IS_NULLABLE " );
		sb.append( "          as CHAR(1)), " );
		sb.append( "              col.ORDINAL_POSITION ," );
		sb.append( "		   col.NUMERIC_SCALE  " );
		sb.append( " from    INFORMATION_SCHEMA.COLUMNS   col, " );
		sb.append( "         INFORMATION_SCHEMA.TABLES          tab " );
		sb.append( " where   tab.TABLE_NAME        = col.TABLE_NAME " );
		sb.append( " and     tab.TABLE_SCHEMA             = col.TABLE_SCHEMA " );
		sb.append( " and     tab.TABLE_SCHEMA             = '" + database + "' " );
		sb.append( " and     tab.TABLE_TYPE             in ('TABLE', 'VIEW') " );

		// MED: Add this back in for Oracle 10g but it won't work on 9i
		//        sb.append( " and     tab.DROPPED           = 'NO' " );

		if ( tableName != null && tableName.length() > 0 )
		{
			sb.append( " and     tab.TABLE_NAME       = '" + tableName.toUpperCase() + "' " );
		}

		sb.append( " order by tab.TABLE_NAME, col.ORDINAL_POSITION" );

		return sb.toString();
	}

	public String getAllTableDetailsQuery( String database, String tableName )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( " select   tab.TABLE_NAME, " );
		sb.append( "          col.COLUMN_NAME, " );
		sb.append( "          col.DATA_TYPE, " );
		sb.append( "              case " );
		sb.append( "                  when col.CHARACTER_MAXIMUM_LENGTH is NULL then col.NUMERIC_PRECISION " );
		sb.append( "                  else col.CHARACTER_MAXIMUM_LENGTH " );
		sb.append( "              end ," );
		sb.append( "          cast( " );
		sb.append( "              col.IS_NULLABLE " );
		sb.append( "          as CHAR(1)), " );
		sb.append( "              col.ORDINAL_POSITION ," );
		sb.append( "		   col.NUMERIC_SCALE  " );
		sb.append( " from    INFORMATION_SCHEMA.COLUMNS   col, " );
		sb.append( "         INFORMATION_SCHEMA.TABLES          tab " );
		sb.append( " where   tab.TABLE_NAME        = col.TABLE_NAME " );
		sb.append( " and     tab.TABLE_SCHEMA             = col.TABLE_SCHEMA " );
		sb.append( " and     tab.TABLE_SCHEMA             = '" + database + "' " );

		// MED: Add this back in for Oracle 10g but it won't work on 9i
		//        sb.append( " and     tab.DROPPED           = 'NO' " );

		if ( tableName != null && tableName.length() > 0 )
		{
			sb.append( " and     tab.TABLE_NAME       = '" + tableName.toUpperCase() + "' " );
		}

		sb.append( " order by tab.TABLE_NAME, col.ORDINAL_POSITION" );

		return sb.toString();
	}

	@Override
	protected List<String> getCreateTableDDL( Table table, String dataLocation ) throws DataSourceException
	{
		Boolean isValidTablespace = isValidTablespaceName( dataLocation );
		if ( isValidTablespace != null && !isValidTablespace )
			throw new DataSourceException( "Invalid Data Location: Invalid Tablespace name '" + dataLocation + "' specified." );

		List<String> sqlList = new ArrayList<String>();
		String statement;

		// Add create table header
		statement = "create table " + table.TableName + StringHelper.NEW_LINE + "(";

		if ( "next_object_no".equals( table.TableName ) )
		{
			String key=" PRIMARY KEY ( ";
			for ( Column col : table.Columns )
			{
				String mandatory = col.isAuditEnabled ? " default " + getDefaultConstant( col ) + " not null," : col.IsPrimaryKey?" not null AUTO_INCREMENT ,":col.IsMandatory ? "not null," : "null    ,";
				if(col.IsPrimaryKey)
					key+=col.ColumnName+" ) ";
				statement += StringHelper.NEW_LINE + "    " + col.ColumnName + " " + columnDataTypeToDbType( col.DataType, col.Length ) + " " + mandatory;
			}
			statement+= key;
		}
		else
		{

			for ( Column col : table.Columns )
			{
				String mandatory = col.isAuditEnabled ? " default " + getDefaultConstant( col ) + " not null," : col.IsMandatory ? "not null," : "null    ,";
				statement += StringHelper.NEW_LINE + "    " + col.ColumnName + " " + columnDataTypeToDbType( col.DataType, col.Length ) + " " + mandatory;
			}
		}

		// Strip off the trailing comma and add close brackets
		statement = statement.substring( 0, statement.length() - 1 ) + StringHelper.NEW_LINE + ")" + StringHelper.NEW_LINE;

		// Add the storage options if specified
		if ( dataLocation != null && dataLocation.length() > 0 )
			statement += " tablespace " + dataLocation;

		// Return the DDL
		sqlList.add( statement );
		return sqlList;
	}

	protected List<String> getDropTableDDL( Table table, boolean checkExists ) throws DataSourceException
	{
		return getDropTableDDL( table.TableName, checkExists );
	}

	protected List<String> getDropTableDDL( String tableName, boolean checkExists ) throws DataSourceException
	{
		List<String> sqlList = new ArrayList<String>();
		String statement;

		// Generate the drop table
		if ( checkExists )
		{
			// Check for existence
			statement = "declare v_count number;" + " begin" + " select count(1) into v_count from all_tables" + " where table_name = '" + tableName.toUpperCase() + "' and owner = '" + getUserName().toUpperCase() + "';" + " if v_count > 0 then" + "    execute immediate 'drop table " + tableName + "';" + "  end if;" + " end;";
		}
		else
		{
			// Just drop the table
			statement = "drop table " + tableName;
		}
		sqlList.add( statement );

		// Return the DDL
		return sqlList;
	}

	public List<String> getDropIndexDDL( Index index, boolean checkExists ) throws DataSourceException
	{
		List<String> sqlList = new ArrayList<String>();
		String statement;

		// Generate the drop index
		statement = "";

		if ( checkExists )
		{
			// Check for existence
			statement = "declare v_count number; " + "begin " + "  select count(1) into v_count from all_indexes" + "  where index_name = '" + index.IndexName.toUpperCase() + "'" + "  and owner = '" + getUserName().toUpperCase() + "'" + "  and table_name = '" + index.table.TableName.toUpperCase() + "';" + "  if v_count > 0 then" + "    execute immediate 'drop index " + index.IndexName + "';" + "  end if;" + "end;";
		}
		else
		{
			// Just drop the index
			statement = "drop index " + index.IndexName;
		}

		sqlList.add( statement );

		// Return the DDL
		return sqlList;
	}

	@Override
	protected List<String> getUpdateTableDDL( Table oldTable, Table newTable, String dataLocation, String indexLocation ) throws DataSourceException
	{

		Boolean isValidTablespace = isValidTablespaceName( dataLocation );
		if ( isValidTablespace != null && !isValidTablespace )
			throw new DataSourceException( "Invalid Data Location: Invalid Tablespace name '" + dataLocation + "' specified." );

		isValidTablespace = isValidTablespaceName( indexLocation );
		if ( isValidTablespace != null && !isValidTablespace )
			throw new DataSourceException( "Invalid Index Location: Invalid Tablespace name '" + indexLocation + "' specified." );

		List<String> sqlList = new ArrayList<String>();

		// Create a temporary migration table based on the new table and add to the schema
		Table migrationTable = null;
		try
		{
			migrationTable = ( Table ) newTable.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new DataSourceException( "Unable to clone table %1", e, newTable.TableName );
		}

		migrationTable.TableName = getMigrationName( newTable );

		// Make sure the migration table doesn't already exist and add to the schema
		if ( newTable.database.Tables.contains( migrationTable ) )
		{
			throw new DataSourceException( "The schema already contains the migration table '%1'", migrationTable.TableName );
		}

		// For Oracle we have to change all the index names too
		for ( Index index : migrationTable.Indexes )
		{
			// Get a migration name - temp_ limited to 30 chars
			String migrationIndexName = "temp_" + index.IndexName.substring( 0, ( ( index.IndexName.length() > 25 ) ? 25 : index.IndexName.length() ) );

			index.IndexName = migrationIndexName;
		}

		newTable.database.Tables.add( migrationTable );

		// Build the sql for the migration table
		sqlList.addAll( getCreateTableDDL( migrationTable, dataLocation ) );

		// Move the rows to the migration table
		// Create the select query handling NULL -> NOT NULL changes
		StringBuilder migrationTableSQ = new StringBuilder();
		migrationTableSQ.append( "select " );

		for ( int i = 0; i < newTable.Columns.size(); i++ )
		{
			// Get the next column
			Column newColumn = newTable.Columns.get( i );
			String selectColumn = "";

			// See if this column exists in the old table or if its definition has changed
			if ( oldTable.Columns.contains( newColumn ) )
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
					selectColumn = "nvl(" + newColumn.ColumnName + ", " + getDefaultConstant( newColumn ) + ")";
				}
			}
			else
			{
				// If mandatory put the default value in otherwise use NULL
				if ( newColumn.IsMandatory )
				{
					// Use the default value
					selectColumn = getDefaultConstant( newColumn );
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
		{
			migrationTableSQ.setLength( migrationTableSQ.length() - 2 );
		}
		migrationTableSQ.append( " from " + oldTable.TableName );

		// Copy the rows from the old table into the migration table
		String migrationTableIQ = "insert into " + migrationTable.TableName + "(" + StringHelper.merge( migrationTable.getColumnNames(), "," ) + ")" + StringHelper.NEW_LINE + migrationTableSQ.toString();

		// Build the sql for the copy of rows to the migration table
		sqlList.add( migrationTableIQ );

		// Drop the old table
		sqlList.addAll( getDropTableDDL( oldTable, false ) );

		// Create the new table
		sqlList.addAll( getCreateTableDDL( newTable, dataLocation ) );

		// Build the insert query
		String newTableIQ = "insert into " + newTable.TableName + "(" + StringHelper.merge( newTable.getColumnNames(), "," ) + ")" + StringHelper.NEW_LINE + "select * from " + migrationTable.TableName;

		// Add to the sql
		sqlList.add( newTableIQ );

		// Remove the temporary migration table from the schema
		newTable.database.Tables.remove( migrationTable );

		// Drop the migration table from the database
		sqlList.addAll( getDropTableDDL( migrationTable, true ) );

		// Return the DDL
		return sqlList;

	}

	@Override
	protected String getAddNewColumnDDL( String fromTable, String newColumn, String dataType ) throws DataSourceException
	{
		return "alter table " + fromTable + " add " + newColumn + " " + dataType;
	}

	@Override
	public String getRenameTableDDL( String fromTable, String toTable ) throws DataSourceException
	{
		return "rename table " + fromTable + " to " + toTable;
	}

	@Override
	protected String getSelectDatabaseDDL( String databaseName ) throws DataSourceException
	{
		return "";
	}

	@Override
	protected void applyAfterTableCreateIndexes( Table table, String dataLocation, String indexLocation ) throws DataSourceException
	{
		applyIndexes( table, indexLocation, false );

	}

	@Override
	public String getDefaultConstant( Column column ) throws DataSourceException
	{
		// Create the default constant based on the type
		switch( column.DataType )
		{
		case Bool:
			return ( isUnicode ? "UNISTR( 'N' )" : "'N'" );
		case DateTime:
			return "to_date('01-JAN-2000')";
		case Int:
		case Long:
		case Decimal:
			return "0";
		case String:
			return ( isUnicode ? "UNISTR( 'X' )" : "'X'" );
		default:
			throw new IllegalArgumentException( "Unknown 'ConstantType': '" + column.DataType + "'" );
		}
	}

	@Override
	public String getColumnRenameDDL( String tableName, String oldColumnName, String newColumnName ) throws DataSourceException
	{
		return "alter table " + tableName + " rename column " + oldColumnName + " to " + newColumnName;
	}

	@Override
	public String getDateTimeConversion( Object value )
	{
		DateTime dateTime = ( DateTime ) value;
		String date = dateTime.toString( NCashDateFormatter.dateFormatStringForStorage );

		return "to_date( '" + date + "','" + NCashDateFormatter.dateFormatStringForMySQlStorage + "')";
	}

	@Override
	public ColumnDataType externaldbTypeToColumnDataType( String dbType, int precision, int scale, boolean ignoreError ) throws DataSourceException
	{
		if ( dbType.toUpperCase().startsWith( "TIMESTAMP" ) || dbType.equalsIgnoreCase( "date" ) )
			return ColumnDataType.DateTime;
		if ( dbType.equalsIgnoreCase( "float" ) || ( dbType.equalsIgnoreCase( "number" ) && scale != 0 ) )
			return ColumnDataType.Decimal;
		if ( dbType.equalsIgnoreCase( "number" ) && precision <= 10 )
			return ColumnDataType.Int;
		if ( dbType.equalsIgnoreCase( "number" ) && precision <= 19 )
			return ColumnDataType.Long;
		if ( dbType.equalsIgnoreCase( "nvarchar2" ) )
			return ColumnDataType.String;
		if ( dbType.equalsIgnoreCase( "varchar2" ) || dbType.equalsIgnoreCase( "varchar" ) || dbType.equalsIgnoreCase( "char" ) || dbType.equalsIgnoreCase( "nchar" ) )
			return ColumnDataType.String;
		if ( ignoreError )
			return ColumnDataType.Unknown;
		else
			throw new DataSourceException( "DB Type '" + dbType + "' not supported" );
	}

	@Override
	protected String getTablespaceQuery( String tablespaceName )
	{
		return "select * from user_tablespaces where tablespace_name = '" + tablespaceName.toUpperCase() + "'";
	}

	@Override
	protected String getAllTablesMatchingNameQuery( String tableNameMatch, boolean likeStart, boolean likeEnd )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "SELECT TNAME FROM TAB WHERE TNAME LIKE '" );
		if ( likeStart )
			sb.append( "%" );

		sb.append( tableNameMatch.toUpperCase() );

		if ( likeEnd )
			sb.append( "%" );

		sb.append( "'" );

		return sb.toString();
	}

	@Override
	protected String getInsertForLoadViaLinkQuery( String fromTableName, String toTableName, String linkToLoadDB )
	{

		return "INSERT INTO " + toTableName + "@" + linkToLoadDB + " SELECT * FROM " + fromTableName;
	}

	@Override
	protected String getInsertForLoadViaLinkQuery( String fromTableName, String toTableName, String fromLink, String toLink )
	{
		String query = "INSERT INTO " + toTableName;
		if ( toLink != null && !toLink.isEmpty() )
			query += "@" + toLink;

		query += " SELECT * FROM " + fromTableName;
		if ( fromLink != null && !fromLink.isEmpty() )
			query += "@" + fromLink;

		return query;
	}

	@Override
	public String getAllViewDetailsQuery( String database, String tableName ) throws DataSourceException
	{
		StringBuilder sb = new StringBuilder();

		sb.append( " select   col.TABLE_NAME, " );
		sb.append( "          col.COLUMN_NAME, " );
		sb.append( "          col.DATA_TYPE, " );
		sb.append( "          cast( " );
		sb.append( "              case " );
		sb.append( "                  when col.CHAR_COL_DECL_LENGTH is NULL then col.DATA_PRECISION " );
		sb.append( "                  else col.CHAR_LENGTH " );
		sb.append( "              end " );
		sb.append( "          as NUMBER(10)), " );
		sb.append( "          cast( " );
		sb.append( "              col.NULLABLE " );
		sb.append( "          as CHAR(1)), " );
		sb.append( "          cast( " );
		sb.append( "              col.COLUMN_ID " );
		sb.append( "          as NUMBER(10)), " );
		sb.append( "		  cast( col.DATA_SCALE as NUMBER(10)) " );
		sb.append( " from    all_tab_columns    col, " );
		sb.append( "         all_objects         obj " );
		sb.append( " where   col.TABLE_NAME        = obj.OBJECT_NAME " );
		sb.append( " and     obj.OWNER             = col.OWNER " );
		sb.append( " and     obj.OWNER             = '" + database.toUpperCase() + "' " );

		// MED: Add this back in for Oracle 10g but it won't work on 9i
		//        sb.append( " and     tab.DROPPED           = 'NO' " );

		if ( tableName != null && tableName.length() > 0 )
		{
			sb.append( " and     obj.OBJECT_NAME       = '" + tableName.toUpperCase() + "' " );
		}

		sb.append( " order by col.TABLE_NAME, cast(col.COLUMN_ID as NUMBER(10))" );

		return sb.toString();
	}

	@Override
	protected List<String> getAlterTableDDL( Table oldTable, Table newTable, String dataLocation ) throws DataSourceException
	{

		Boolean isValidTablespace = isValidTablespaceName( dataLocation );
		if ( isValidTablespace != null && !isValidTablespace )
			throw new DataSourceException( "Invalid Data Location: Invalid Tablespace name '" + dataLocation + "' specified." );

		List<String> sqlList = new ArrayList<String>();

		// Create the select query handling NULL -> NOT NULL changes
		StringBuilder addColsDDL = new StringBuilder();
		StringBuilder dropDefaultDDL = new StringBuilder();
		boolean hasMandatoryCols = false;

		addColsDDL.append( "ALTER TABLE " + newTable.TableName + " ADD ( " );
		dropDefaultDDL.append( "ALTER TABLE " + newTable.TableName + " MODIFY ( " );
		for ( int i = 0; i < oldTable.Columns.size(); i++ )
		{
			Column oldColumn = oldTable.Columns.get( i );
			Column newColumn = null;
			// See if this column exists in the old table or if its definition has changed
			if ( ( newColumn = newTable.Columns.get( i ) ) != null )
				if ( !oldColumn.equals( newColumn ) )
					return null;
		}

		for ( int i = oldTable.Columns.size(); i < newTable.Columns.size(); i++ )
		{
			// Get the next column
			Column newColumn = newTable.Columns.get( i );

			String mandatory = "";
			if ( newColumn.IsMandatory )
			{
				hasMandatoryCols = true;
				mandatory = " DEFAULT " + getDefaultConstant( newColumn ) + " NOT NULL ,";
				dropDefaultDDL.append( StringHelper.NEW_LINE + "    " + newColumn.ColumnName + "    " + columnDataTypeToDbType( newColumn.DataType, newColumn.Length ) + " DEFAULT NULL ," );
			}
			else
				mandatory = "NULL    ,";
			addColsDDL.append( StringHelper.NEW_LINE + "    " + newColumn.ColumnName + "    " + columnDataTypeToDbType( newColumn.DataType, newColumn.Length ) + " " + mandatory );
		}

		String statement = addColsDDL.toString();
		statement = statement.substring( 0, statement.length() - 1 ) + "  ) " + StringHelper.NEW_LINE;

		//Add the storage options if specified
		if ( dataLocation != null && dataLocation.length() > 0 )
			statement += " tablespace " + dataLocation;
		sqlList.add( statement );

		if ( hasMandatoryCols )
		{
			statement = dropDefaultDDL.toString();
			if ( !"".equals( statement ) )
				statement = statement.substring( 0, statement.length() - 1 ) + "  ) " + StringHelper.NEW_LINE;

			if ( dataLocation != null && dataLocation.length() > 0 )
				statement += " tablespace " + dataLocation;
			sqlList.add( statement );
		}

		// Return the DDL
		return sqlList;

	}

}
