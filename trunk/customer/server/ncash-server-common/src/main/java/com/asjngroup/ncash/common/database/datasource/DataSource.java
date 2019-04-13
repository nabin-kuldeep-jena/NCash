package com.asjngroup.ncash.common.database.datasource;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.database.helper.IdGenerator;
import com.asjngroup.ncash.common.database.schema.Column;
import com.asjngroup.ncash.common.database.schema.ColumnDataType;
import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.database.schema.Table;

public interface DataSource
{
    public static final String BCP_FIELD_DELIMITER = "\t";

    public final static String DATASOURCE = "ncash.datasource";
    public final static String DATABASE = "ncash.database";
    public final static String UNICODE = "ncash.unicode";

    public void decorateResultSet(ResultSet rs);

	public DatabaseType getDatabaseType();

    public Connection getConnection() throws DataSourceException;

    public void closeAll();
    
    public void close(Connection connection);

    public DateTimeFormatter getBcpDateFormatter();

    public DateTimeFormatter getBcpDateTimeFormatter();
    public DateTimeFormatter getBcpMilliDateTimeFormatter();

    public String getSqlDateConstant( DateTime dateTime );

    public String getSqlDateConstant( DateTime dateTime, DateTimeFormatter dateTimeFormatter );

    public Charset getBcpCharset();

    public String getUniqueConnectionId( Connection connection ) throws DataSourceException;

    public String getDifferenceOperator();

    public List< String > getTableNames( String database ) throws DataSourceException;

    public List< String > getTableNames( String database, String tablePattern ) throws DataSourceException;

    public void truncateTable( String database, String tableName ) throws DataSourceException;

    public void truncateTable( String database, String tableName, boolean executeWithWait ) throws DataSourceException;

    public void createTable( Table table, String dataLocation, String indexLocation ) throws DataSourceException;
    
    public void alterTableWithNoLogging( Table table  ) throws DataSourceException;

    public void createTable( Table table, String dataLocation, String indexLocation, boolean createIndexes ) throws DataSourceException;

    public void dropTable( Table table ) throws DataSourceException;

    public void dropTableSilent( Table table );

    public void dropTable( String tableName ) throws DataSourceException;

    public void dropTableSilent( String tableName );

    public void recreateTable( Table table, String dataLocation, String indexLocation ) throws DataSourceException;

    public void dropTableWarn( Table table );

    public void updateTable( Table oldTable, Table newTable, String dataLocation ) throws DataSourceException;

    public void updateIndex( Index oldIndex, Index newIndex, String indexLocation ) throws DataSourceException;

    public void renameTable( String oldTableName, String newTableName ) throws DataSourceException;

    public void renameColumn(String tableName,String oldColumnName,String newColumnName) throws DataSourceException;
    public void copyTable( Table oldTable, String newTableName ) throws DataSourceException;

    public boolean tableExists( String tableName ) throws DataSourceException;

    public void dropAllIndexes( Table table ) throws DataSourceException;

    public void applyIndexes( Table table, String indexLocation ) throws DataSourceException;

    public void applyIndex( Index index, String indexLocation ) throws DataSourceException;

    public void dropAllIndexes() throws DataSourceException;

    public void applyIndexes( String indexLocation ) throws DataSourceException;

    public void updateNextNumber( Table table, String objectName ) throws DataSourceException;

    public void updateNextNumber( String tableName, String primaryKeyColumn, String objectName ) throws DataSourceException;

    public Table getTableWithoutIndexes( String database, String table ) throws DataSourceException;
    public void updateAllNextNumber() throws DataSourceException;

    public void dropStoredProcedure( String procName ) throws DataSourceException;

    public void applyStoredProcedure( Map< String, String > map, String procName, String macro ) throws DataSourceException;

    public List< String[] > executeStoredProcedure( String procName, List args ) throws DataSourceException;

    public String getDataSourceName();

    public javax.sql.DataSource getDataSource();

    public String getDatabase();

    public boolean isUnicode();

    public void attachSchema( Schema schema );

    public Schema getSchema();

    public Schema getSchema( String database, String table ) throws DataSourceException;

    public Schema getSchema( String database, String table, boolean syncWithNonSpark ) throws DataSourceException;

    public Schema getViewSchema( String database, String table, boolean syncWithNonSpark ) throws DataSourceException;

    public Schema getSchema( String database ) throws DataSourceException;

    public IdGenerator getIdGenerator();

    public String getSelectString(String tableName, String[] colNames);

    public String getSelectString(String tableName, String[] colNames, String whereClause );

    public String getSqlToFetchNRows(String tableName, long maxRows);

    public String getSelectString(String tableName, String[] colNames, String orderByClause, long min, long max);

    public String getSelectString(String string, String[] colNames, String orderByClause, String whereClause, long min, long max);

    public String getPaginatedSelectString ( String tableName, String[] colNames , long batchSize );

    public String getPaginatedSelectStringWithOrderBy ( String tableName, String[] colNames, String[] orderByColumns, long batchSize );

  //  public ChangedClassLogger getChangedClassLogger();

    // bit of a hack. patches a schema up for differences in database types. eg we don't use clustered
    // indexes in oracle right now so the source schema needs to have the clustered indexes set to
    // normal indexes.
    public Schema preCompareSchemaPatch( Schema schema ) throws DataSourceException;

    // escapes a string parameter to avoid unwanted wildcard matching sequences -
    // '%' and '_' should be passed through untouched
    public String escapeString( String s );

    public ColumnDataType externalDBTypeToColumnDataType( String dbType, int precision, int scale, boolean ignoreError ) throws DataSourceException;

    public String getAllTableDetailsQuery( String database, String tableName ) throws DataSourceException;

    public void addNewColumn(String table,String newColumn,String dataType) throws DataSourceException;

    public void dropColumn(String table,String column) throws DataSourceException;

    public String createSQLInsertQuery( String table, List<Column> columns);

    public String createSQLSelectQuery( String tableName, List<Column> selectColumns, List<Column> whereColumns);

    public String convertDateTimeToDBFormat (Column column);

    public String createSQLUpdateQuery (String s, List<Column> columnList,List<Column> whereColumnList);
	public String getOrderByClause(List<Object[]> colNames, ColumnDataType[] resultTypes) throws Exception;

	public String getSelectString(String tableName, String[] colNames, long min, long max);

	public abstract String getFunctionName( String functionName ) throws DataSourceException;

	public abstract String getIsNullFunctionName();


	public abstract boolean doesColumnExist(String tableName, String columnName) throws DataSourceException;

	public String toChar(String column, ColumnDataType dataType);

	public String mod(String column, int divider);

	public String getCaseStatement(String column, String ...conditions) throws DataSourceException;

	public String hash(List<Column> columns);

	public String getConcatOperator();

	public String createTableFromAnotherTable(String newTable, String oldTable, String[] colNames);

	public String createTableFromAnotherTable(String newTable, String oldTable, String[] colNames, String whereClause);

	public String getNullSafeFormattedColumn(String columnName, ColumnDataType type);

	public String getFormattedColumn(String columnName, ColumnDataType type);

	public List< String > getDropIndexDDL( Index index, boolean checkExists ) throws DataSourceException;

	public String getHybridDataCopyQuery(String fromTableName, String toTableName);

	public String getDropIndexDDL(String tinTableName, String indexName, boolean checkExists) throws DataSourceException;

	public String getCreateIndexDDL(String tinTableName, String indexName,boolean uniqueFl, String columnList, String indexLocation) throws DataSourceException;

	public String getRenameTableDDL(String fromTable, String toTable, boolean checkExists) throws DataSourceException;

	public String getRenameTableDDL(String fromTable, String toTable ) throws DataSourceException;

	public List<String> getDropTable(String tableName, boolean checkExists) throws DataSourceException;
	public List<String> getDropTable(Table tableName, boolean checkExists) throws DataSourceException;

	public String getTruncateTable(String tableName) throws DataSourceException;

	public void dropColumnNullConstraint(Connection connection, String tableName, String colName, String optDataType) throws DataSourceException;

	public String getUnicodeCharset();

	public String getSqlConvert(ColumnDataType toType, int length, String columnName) throws DataSourceException;

	public String getTrimFunction(String columnName);
	public String convertDateTimeToDate(String columnWithPrefix) throws DataSourceException;

	public String convertDateTimeToDateWithHour(String columnWithPrefix) throws DataSourceException;

	public String getHashFunctionQuery(String hashTableColumnName,Integer totolThreadCount,Integer threadNumber);


	public void disableIndexesAndConstraintsFor(String toTableName, Connection conn, String database) throws SQLException, DataSourceException;

	public void enableIndexesAndConstraintsFor(String toTableName, Connection conn, String database) throws SQLException, DataSourceException;

	public String getSqlConvert( ColumnDataType to, int length, Column from ) throws DataSourceException;

	public String getDefaultConstant( Column column ) throws DataSourceException;

	public Boolean isValidTablespaceName( String tablespaceName ) throws DataSourceException;

	public void createExternalTable( Table table, String dataLocation, String directory, String filename ) throws DataSourceException;


	public void dropAllTablesMatchingName( String tableNameMatch, boolean likeStart, boolean likeEnd ) throws DataSourceException;
	public String getDateTimeConversion( Object value );
	public Boolean lockForUpdate( Connection con, String lockForUpdateQuery ) throws DataSourceException;

	public void executeBatch( Connection conn, Statement stmt, ArrayList<String> queriesList ) throws DataSourceException;

	public void createExternalTable( Table table, String dataLocation, String directory, List<File> files ) throws DataSourceException;

}
