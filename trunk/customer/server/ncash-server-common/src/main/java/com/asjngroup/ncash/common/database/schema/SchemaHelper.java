package com.asjngroup.ncash.common.database.schema;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.database.datasource.DataSource;
import com.asjngroup.ncash.common.database.hibernate.references.TableColumn;
import com.asjngroup.ncash.common.database.hibernate.references.TableDfn;
import com.asjngroup.ncash.common.database.hibernate.references.TableIndex;
import com.asjngroup.ncash.common.database.hibernate.references.TableIndexColumn;
import com.asjngroup.ncash.common.database.hibernate.references.TableInst;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateTransaction;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;

public class SchemaHelper
{
    public static final int MAX_TABLE_NAME_LENGTH = 26;
    public static final int MAX_AUDITING_TABLE_NAME_LENGTH = 29;
    public static final int MAX_INDEX_NAME_LENGTH = 30;
    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    public static Schema buildSchema( String schemaPath ) throws SchemaParseException
    {
        InputStream inputStream = SchemaHelper.class.getResourceAsStream( schemaPath );

        if ( inputStream == null )
        {
            throw new SchemaParseException( "Could not find schema file %1", schemaPath );
        }
        return Schema.loadSchema( inputStream );
    }

    public static Schema buildSchema( List< String > schemaPaths ) throws SchemaParseException
    {
        if ( schemaPaths.size() == 0 )
        {
            throw new IllegalArgumentException( "Must supply at lease one schema file" );
        }

        Schema schema = null;
        for ( String schemaPath : schemaPaths )
        {
            if ( schema == null )
            {
                schema = buildSchema( schemaPath );
            }
            else
            {
                schema.mergeSchema( buildSchema( schemaPath ) );
            }
        }

        return schema;
    }

    public static Map< String, Map< String, List< String > > > buildUniqueIndexMap( Schema schema )
    {
        Map< String, Map< String, List< String > > > indexMap = new HashMap< String, Map< String, List< String > > >();

        for ( Database database : schema.Databases )
        {
            for ( Table table : database.Tables )
            {
                EntityMapping entityMapping = schema.findEntityMappingFromTableName( database.DatabaseName, table.TableName );

                // skip if there is no object mapping
                if ( entityMapping == null )
                    continue;

                Map< String, List< String > > indexes = new HashMap< String, List< String >>();

                for ( Index index : table.Indexes )
                {
                    // ignore non-unique indexes
                    if ( !index.IsUnique )
                        continue;

                    List< String > indexProperties = new ArrayList< String >();

                    String[] indexColumns = StringHelper.split( index.ColumnList, ',' );
                    for ( String indexColumn : indexColumns )
                    {
                        FieldMapping fieldMapping = entityMapping.findFieldMappingByColumn( indexColumn );
                        indexProperties.add( fieldMapping.FieldName );
                    }

                    indexes.put( index.IndexName, indexProperties );
                }

                indexMap.put( entityMapping.EntityName, indexes );
            }
        }

        return indexMap;
    }

    public static void saveToV5SchemaFile( Schema schema, String filename ) throws IOException
    {
        PrintWriter writer = new PrintWriter( new FileOutputStream( filename ) );
        writer.println( "<SchemaRoot>" );
        writer.println( "  <Schema Name=\"" + schema.SchemaName + "\" MajorVersion=\"" + schema.MajorVersion + "\" MinorVersion=\"" + schema.MinorVersion + "\" RevisionVersion=\"" + schema.RevisionVersion + "\" ServicePack=\""
                + schema.ServicePack + "\" IsExtension=\"0\">" );
        writer.println( "    <Databases>" );
        for ( Database database : schema.Databases )
        {
            writer.println( "      <Database Database=\"" + database.DatabaseName + "\" ExtensionKey=\"SCHEMA\">" );
            writer.println( "        <Tables>" );
            for ( Table table : database.Tables )
            {
                writer.println( "          <Table Table=\"" + table.TableName + "\" MaxRowsPerPage=\"0\" NoTrigger=\"-1\" ExtensionKey=\"SCHEMA\">" );
                writer.println( "            <Columns>" );
                for ( Column column : table.Columns )
                {
                    String datatype = getV5Datatype( column );
                    writer.println( "              <Column Column=\"" + column.ColumnName + "\" Datatype=\"" + datatype + "\" Mandatory=\"" + ( column.IsMandatory ? "-1" : "0" ) + "\" IsPrimaryKey=\"" + ( column.IsPrimaryKey ? "-1" : "0" )
                            + "\" ExtensionKey=\"SCHEMA\" />" );
                }
                writer.println( "            </Columns>" );
                // Ignoring indexes...
                writer.println( "          </Table>" );
            }
            writer.println( "        </Tables>" );
        }
        writer.println( "      </Database>" );
        writer.println( "    </Databases>" );
        writer.println( "    <Datatypes>" );
        writer.println( "      <Datatype Datatype=\"dt_id\" StorageType=\"int\" Precision=\"0\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "      <Datatype Datatype=\"dt_identity\" StorageType=\"numeric\" Precision=\"12\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "      <Datatype Datatype=\"dt_desc\" StorageType=\"varchar\" Precision=\"255\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "      <Datatype Datatype=\"dt_datetime\" StorageType=\"datetime\" Precision=\"0\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "      <Datatype Datatype=\"dt_flag\" StorageType=\"char\" Precision=\"1\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "      <Datatype Datatype=\"dt_amount\" StorageType=\"numeric\" Precision=\"12\" Skale=\"0\" Rule=\"\" DoNotGenerate=\"0\" ExtensionKey=\"SCHEMA\" />" );
        writer.println( "    </Datatypes>" );
        writer.println( "  </Schema>" );
        writer.println( "</SchemaRoot>" );
        writer.close();
    }

    private static String getV5Datatype( Column column )
    {
        switch (column.DataType)
        {
        case Long:
            return "dt_identity";
        case Int:
            return "dt_id";
        case String:
            return "dt_desc";
        case DateTime:
            return "dt_datetime";
        case Bool:
            return "dt_flag";
        case Decimal:
            return "dt_amount";
        }
        throw new IllegalArgumentException( "Bad datatype: " + column.DataType );
    }

    public static String getPrimaryKeyColumnName( String tableName )
    {
        List< TableInst > tableInsts = null;
        try
        {
            tableInsts = HibernateSession.query( "from TableInst tin where tin.TinTableName = :tableName", "tableName", tableName );
        }
        catch ( HibernateException e )
        {
            throw new NCashRuntimeException( e );
        }

        if ( tableInsts.size() == 0 )
            throw new NCashRuntimeException( "Table '%1' is not a valid table instance", tableName );
        List< String > tableColumns = null;

        try
        {
            tableColumns = HibernateSession.query( "select tcl.TclName from TableColumn tcl, TableInst tin where tcl.TbdId = tin.TbdId and tin.TinTableName = :tableName and tcl.TclPrimaryKeyFl = true", "tableName", tableName );
        }
        catch ( HibernateException e )
        {
            throw new NCashRuntimeException( e );
        }

        if ( tableColumns.size() == 0 )
            throw new NCashRuntimeException( "Table '%1' does not have a primary key column", tableName );

        return tableColumns.get( 0 );
    }

    public static ColumnDataType[] getColumnDataTypesForColumns( String tableName, String[] columnNames ) throws HibernateException
    {
        List< TableInst > tableInsts = HibernateSession.query( "from TableInst tin where tin.TinTableName = :tableName", "tableName", tableName );

        if ( tableInsts.size() == 0 )
            throw new NCashRuntimeException( "Table '%1' is not a valid table instance", tableName );

        List< TableColumn > tableColumns = HibernateSession.query( "select tcl from TableColumn tcl, TableInst tin where tcl.TbdId = tin.TbdId and tin.TinTableName = :tableName", "tableName", tableName );

        ColumnDataType[] ColumnDataTypes = new ColumnDataType[columnNames.length];
        int i = 0;
        for ( String columnName : columnNames )
        {
            boolean foundColumn = false;

            for ( TableColumn tableColumn : tableColumns )
            {
                if ( columnName.equals( tableColumn.getTclName() ) )
                {
                    ColumnDataTypes[ i++ ] = ColumnDataTypeHelper.typeStringToColumnDataType( tableColumn.getTclType() );
                    foundColumn = true;
                    break;
                }
            }

            // check the column exists
            if ( !foundColumn )
                throw new NCashRuntimeException( "Column '%1' is not a valid column name for table '%2'", columnName, tableName );
        }

        return ColumnDataTypes;
    }

    public static ColumnDataType getColumnDataTypesForColumn( String tableName, String columnName ) throws HibernateException
    {
        ColumnDataType[] ColumnDataTypes = getColumnDataTypesForColumns( tableName, new String[] { columnName } );

        return ColumnDataTypes[ 0 ];
    }

    public static Table getTableFromTableInst( String tableName ) throws HibernateException
    {
    	Integer tableInst = HibernateSession.queryExpectOneRow( "select tin.TinId from TableInst tin where tin.TinTableName = :tableName", "tableName", tableName );

        return getTableFromTableInst( tableInst, null, true );
    }

    public static Table getTableFromTableInst( int tinId ) throws HibernateException {
    	return getTableFromTableInst( tinId, null, true );
    }

    public static Table getTableFromTableInst( int tinId, Boolean forRecentData, Boolean indexFl ) throws HibernateException
    {
        // get the table inst and dfns
        TableInst tableInst = HibernateSession.get( TableInst.class, tinId );
        TableDfn tableDfn = HibernateSession.get( TableDfn.class, tableInst.getTbdId() );

        // get the columns
        List< TableColumn > tableColumns = HibernateSession.query( " from TableColumn tcl " + " where tcl.TbdId = :tbdId " + " order by tcl.TclOrderNo ", "tbdId", tableInst.getTbdId() );


        // create the table object
        Table table = new Table();
        table.TableName = tableInst.getTinTableName();
        table.TablePrefix = tableDfn.getTbdPrefix();
        table.TbdId = tableInst.getTbdId();
        table.TinId = tableInst.getTinId();
        table.PtnId = tableInst.getPartitionId();

        // add the columns
        Map< Integer, String > tableColumnMap = new HashMap< Integer, String >();
        for ( TableColumn tableColumn : tableColumns )
        {
            // create a column object
            Column column = getColumnFromTableColumn( tableColumn );

            // attach to the table
            column.table = table;
            table.Columns.add( column );

            // add to the map we use later for the indexes
            tableColumnMap.put( tableColumn.getTclId(), tableColumn.getTclName() );
        }

        table.TableColumnMap = tableColumnMap;

        if( indexFl != null && indexFl )
        {
        	// get the indexes
        	String query = " from TableIndex tix  where tix.TbdId = :tbdId" ;

        	if(forRecentData != null && forRecentData)
        		query += " and tix.TixRecentDataFl = " + forRecentData;
        	else if( forRecentData != null )
        		query += " and tix.TixOlderDataFl = " +  !forRecentData;
        	List< TableIndex > tableIndexes = HibernateSession.query( query, "tbdId", tableDfn.getTbdId() );
        	// add the indexes
        	for ( TableIndex tableIndex : tableIndexes )
        	{
        		// create an index object
        		Index index = new Index();
        		index.IndexName = tableInst.getTinTableName() + tableIndex.getTixSuffix();
        		index.IsUnique = tableIndex.getTixUniqueFl();
        		index.IsClustered = tableIndex.getTixClusteredFl();
        		index.IsBusinessConstraint = false;
        		index.IsIgnoreDupKey = false;
        		index.IsOlderData = tableIndex.getTixOlderDataFl();
        		index.IsRecentData = tableIndex.getTixRecentDataFl();
        		index.TixId = tableIndex.getTixId();

        		// get the list of columns in the index
        		List< TableIndexColumn > tableIndexColumns = HibernateSession.query( " from TableIndexColumn tic " + " where tic.TixId = :tixId " + " order by tic.TicOrderNo ", "tixId", tableIndex.getTixId() );

        		// build and set the comma-separated string list of columns
        		String columnList = "";
        		for ( TableIndexColumn tableIndexColumn : tableIndexColumns )
        		{
        			// get the column to access it's name
        			String tclName = tableColumnMap.get( tableIndexColumn.getTclId() );
        			columnList += tclName + ",";
        		}
        		if ( columnList.length() > 0 )
        			columnList = columnList.substring( 0, columnList.length() - 1 );
        		index.ColumnList = columnList;

        		// attach to the table
        		index.table = table;
        		table.Indexes.add( index );
        	}
        }

        // return the constructed table object
        return table;
    }

    public static Column createColumn( String name, int seqNo, ColumnDataType type )
    {
        Column column = new Column();
        column.ColumnName = name;
        column.SequenceNumber = seqNo;
        column.DataType = type;
        column.Length = type.getLength();
        column.IsMandatory = false;
        column.IsPrimaryKey = false;

        return column;
    }

    public static Column getColumnFromTableColumn( TableColumn tableColumn )
    {
        Column column = new Column();
        column.ColumnName = tableColumn.getTclName();
        column.SequenceNumber = tableColumn.getTclOrderNo();
        column.DataType = ColumnDataTypeHelper.typeStringToColumnDataType( tableColumn.getTclType() );
        column.Length = tableColumn.getTclLength();
        column.IsMandatory = tableColumn.getTclMandatoryFl();
        column.IsPrimaryKey = tableColumn.getTclPrimaryKeyFl();

        return column;
    }

    public static TableColumn tableColumnFromColumn( Column column ) throws HibernateException
    {
        TableColumn tableColumn = HibernateSession.createObject( TableColumn.class );
        tableColumn.setTclLength( column.Length );
        tableColumn.setTclMandatoryFl( column.IsMandatory );
        tableColumn.setTclName( column.ColumnName );
        tableColumn.setTclDisplay( column.ColumnName );
        tableColumn.setTclOrderNo( column.SequenceNumber );
        tableColumn.setTclPrimaryKeyFl( column.IsPrimaryKey );
        tableColumn.setTclType( column.DataType.toTableColumnType() );

        return tableColumn;
    }

    public static TableDfn tableDfnFromTable( Table table ) throws HibernateException
    {
        TableDfn tableDfn = HibernateSession.createObject( TableDfn.class );
        tableDfn.setTbdName( table.TableName );
        tableDfn.setTbdPrefix( table.TablePrefix );

        return tableDfn;
    }

    public static TableInst saveTableInstFromTable( Session session, Table table )
    {
        try
        {
            TableDfn tableDfn = tableDfnFromTable( table );
            tableDfn.setTbdInternalFl( true );

            session.save( tableDfn );

            for ( Column column : table.Columns )
            {
                TableColumn tableColumn = tableColumnFromColumn( column );
                tableColumn.setTbdId( tableDfn.getTbdId() );
                session.save( tableColumn );
            }

            TableInst tableInst = HibernateSession.createObject( TableInst.class );
            tableInst.setTbdId( tableDfn.getTbdId() );
            tableInst.setTinAlias( tableDfn.getTbdPrefix() );
            tableInst.setTinDisplayName( tableDfn.getTbdName() );
            tableInst.setTinTableName( tableDfn.getTbdName() );

            session.save( tableInst );

            return tableInst;
        }
        catch ( HibernateException e )
        {
            throw new NCashRuntimeException( e );
        }
    }

    public static TableDfn saveTableDfnFromTable( SessionFactory sessionFactory, Table table ) throws HibernateException
    {
        HibernateTransaction transaction = new HibernateTransaction( sessionFactory );

        TableDfn tableDfn = tableDfnFromTable( table );
        transaction.save( tableDfn );

        for ( Column column : table.Columns )
        {
            TableColumn tableColumn = tableColumnFromColumn( column );
            tableColumn.setTbdId( tableDfn.getTbdId() );
            transaction.save( tableColumn );
        }

        transaction.commit();

        return tableDfn;
    }

    public static String getDefaultColumnAlignment( TableColumn tclObj )
    {
        if ( tclObj.getTclType().equals( "decimal" ) || tclObj.getTclType().equals( "int" ) || tclObj.getTclType().equals( "integer" ) || tclObj.getTclType().equals( "long" ) )
            return "right";

        return "left";
    }

    // return a name that always preserves the suffix but will right trim the
    // table prefix if the table prefix + the suffix is too long
    public static String toSafeSchemaObjectName( String prefix, String suffix, int maxLength )
    {
        // allow a null suffix
        if ( suffix == null )
            suffix = "";

        if ( suffix.length() > maxLength )
            throw new IllegalArgumentException( "Suffix is greater than maximum length:" + maxLength );

        int totalLength = prefix.length() + suffix.length();

        // if <= max characters just return ok
        if ( totalLength <= maxLength )
        {
            return prefix + suffix;
        }

        int trimSize = totalLength - maxLength;

        // too large, trim the prefix
        return prefix.substring( 0, prefix.length() - trimSize ) + suffix;
    }

    public static String toSafeSchemaTableName( String prefix, String suffix )
    {
        return toSafeSchemaObjectName( prefix, suffix, MAX_TABLE_NAME_LENGTH );
    }

    public static String toSafeSchemaIndexName( String prefix, String suffix )
    {
        return toSafeSchemaObjectName( prefix, suffix, MAX_INDEX_NAME_LENGTH );
    }

    public static String toSafeSchemaColumnName( String prefix, String suffix )
    {
        return toSafeSchemaObjectName( prefix, suffix, MAX_COLUMN_NAME_LENGTH );
    }

    public static void validateTable( Table table )
    {
        if ( table.TableName.length() > 4 && table.TableName.substring( table.TableName.length() - 4, table.TableName.length() ).equals( "_adt" ) )
        {
            if ( table.TableName.length() > MAX_AUDITING_TABLE_NAME_LENGTH )
                throw new NCashRuntimeException( "Table name '%1' is too long (max %2 chars supported)", table.TableName, MAX_AUDITING_TABLE_NAME_LENGTH );
        }
        else
        {
            if ( table.TableName.length() > MAX_TABLE_NAME_LENGTH )
                throw new NCashRuntimeException( "Table name '%1' is too long (max %2 chars supported)", table.TableName, MAX_TABLE_NAME_LENGTH );
        }

        for ( Column column : table.Columns )
        {
            validateColumn( column );
        }

        for ( Index index : table.Indexes )
        {
            validateIndex( index );
        }
    }

    public static void validateIndex( Index index )
    {
        if ( index.IndexName.length() > MAX_INDEX_NAME_LENGTH )
            throw new NCashRuntimeException( "Index name '%1' is too long (max %2 chars supported)", index.IndexName, MAX_INDEX_NAME_LENGTH );
    }

    public static void validateColumn( Column column )
    {
        if ( column.ColumnName.length() > MAX_COLUMN_NAME_LENGTH )
            throw new NCashRuntimeException( "Column name '%1' is too long (max %2 chars supported)", column.ColumnName, MAX_COLUMN_NAME_LENGTH );
    }

    public static String[] getTableColumnNames( Table table )
    {
        String[] names = new String[table.Columns.size()];
        for ( int i = 0; i < table.Columns.size(); i++ )
        {
            // add the name to the result array
            names[ i ] = table.Columns.get( i ).ColumnName;
        }

        return names;
    }

    public static ColumnDataType[] getTableColumnTypes( Table table )
    {
        ColumnDataType[] resultTypes = new ColumnDataType[table.Columns.size()];
        for ( int i = 0; i < table.Columns.size(); i++ )
        {
            // add the type to the result array
            resultTypes[ i ] = table.Columns.get( i ).DataType;
        }

        // return the type array
        return resultTypes;
    }

    public static ColumnDataType[] getTableColumnTypes( int tbdId )
    {
        // get the list of table columns
        List< TableColumn > tableColumns = null;
        try
        {
            tableColumns = HibernateSession.query( " from TableColumn tcl " + " where tcl.TbdId = :tbdId " + " order by tcl.TclOrderNo ", "tbdId", tbdId );
        }
        catch ( HibernateException e )
        {
            throw new NCashRuntimeException( e );
        }

        // build the array of spark result types
        ColumnDataType[] resultTypes = new ColumnDataType[tableColumns.size()];
        for ( int i = 0; i < tableColumns.size(); i++ )
        {
            // add the type to the result array
            resultTypes[ i ] = ColumnDataTypeHelper.typeStringToColumnDataType( tableColumns.get( i ).getTclType() );
        }

        // return the type array
        return resultTypes;
    }

    public static void cleanupTableInst( Session session, int tinId ) throws HibernateException
    {
        TableInst tableInst = HibernateSession.get( TableInst.class, tinId );
        session.delete( tableInst );

        cleanupTableDfn( session, tableInst.getTbdId() );
    }

    public static void cleanupTableDfn( Session session, int tbdId ) throws HibernateException
    {
        TableDfn tableDfn = HibernateSession.get( TableDfn.class, tbdId );
        List< TableColumn > tableColumns = HibernateSession.query( "from TableColumn tcl where tcl.TbdId = :tbdId", "tbdId", tbdId );

        session.delete( tableDfn );

        for ( TableColumn tableColumn : tableColumns )
            session.delete( tableColumn );

    }

    public static List< TableColumn > getTableColumns( int tbdId ) throws HibernateException
    {
        return HibernateSession.query( "select tcl from TableColumn tcl order by tcl.TclOrderNo where tcl.TbdId = :tbdId", "tbdId",new Integer( tbdId) );
    }

    public static int getUsageGroupId( String tableName )
    {
        try
        {
            Integer usgId = (Integer)HibernateSession.queryExpectOneRow( "select tin.SchemaTbl.UsgId from TableInst tin where tin.TinTableName = :tinTableName", "tinTableName", tableName );

            if ( usgId == null )
                throw new NCashRuntimeException( "Table '%1' is not a partitioned table", tableName );

            return usgId;
        }
        catch ( HibernateException e )
        {
            throw new NCashRuntimeException( "Failed to find the Table Instance with TinTableName = '%1'", tableName );
        }
    }

    public static ColumnDataType[] getColumnDataTypesForDBColumns(DataSource dataSource, String tableName, String[] colNames) throws Exception
    {
    	return getColumnDataTypesForDBColumns(dataSource, tableName, colNames, false);
    }

    public static ColumnDataType[] getColumnDataTypesForDBColumns(DataSource dataSource, String tableName, String[] colNames, boolean ignoreError) throws Exception
    {
    	String sqlString = dataSource.getSelectString(tableName, colNames);
    	Connection conn = null;
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	ColumnDataType[] ColumnDataTypes = null;
    	try
    	{
    		conn = dataSource.getConnection();
    		stmt = (PreparedStatement)conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
			ResultSetMetaData rsm = rs.getMetaData();
			ColumnDataTypes = new ColumnDataType[colNames.length];
			for (int i = 0, j = 0; i < rsm.getColumnCount(); i++)
			{
				if(Types.CLOB != rsm.getColumnType(i+1) && Types.BLOB != rsm.getColumnType(i+1))
					ColumnDataTypes[j] = dataSource.externalDBTypeToColumnDataType(rsm.getColumnTypeName(i+1), rsm.getPrecision(i+1), rsm.getScale(i+1), ignoreError);
				else
					ColumnDataTypes[j] = dataSource.externalDBTypeToColumnDataType(rsm.getColumnTypeName(i+1), 0, rsm.getScale(i+1), ignoreError);
				j++;
			}
		}
    	finally
    	{
    		if(rs != null)
    			rs.close();
    		if(stmt != null)
    			stmt.close();
    		if(conn != null)
    			conn.close();
    	}
    	return ColumnDataTypes;
    }


	public static class BaseConverter
	{
		public static String toBase36String( String s )
		{
			return ( new BigInteger( s, 10 ) ).toString( 36 );
		}

		public static String fromBase36String( String s )
		{
			return ( new BigInteger( s, 36 ) ).toString( 10 );
		}
	}

	public static String getBase36Equivalent( String decimal )
	{
		return BaseConverter.toBase36String( decimal.toString() );
	}

	public static String getTempTableName( Integer intId, Long longId )
	{
		return "tmp_" + BaseConverter.toBase36String( intId.toString() ) + "_" + BaseConverter.toBase36String( longId.toString() );
	}

	public static String getTempTableName( String prefix, Integer intId, Long longId )
	{
		return prefix + BaseConverter.toBase36String( intId.toString() ) + "_" + BaseConverter.toBase36String( longId.toString() );
	}

	/*public static Schema addAuditTables( Schema schema ) throws SchemaException
    {
        // merge audit changes in to prevent the sync dropping the audit column
		List<Object[]> auditLevels;
		try {
			auditLevels = HibernateSession.find("select aul.EntityTbl.EntEntity, aul.AulLevel from AuditLevel aul");
		} catch (HibernateException e) {
			throw new SchemaException(e);
		}

		// loop over all the audit levels and apply to the schema
		for (Object[] row : auditLevels) {
			String entityName = (String) row[0];
			int level = (Integer) row[1];

			if (Auditing.hasFieldAuditing(level)) {
				// NOTE: the no object mapping will exist if the object no
				// longer exists in the schema
				EntityMapping om = schema.findEntityMapping(entityName);

				if (om != null) {
					// Get the table
					Table table = om.getTable();

					// add all the audit columns to the table
					table.setFieldAuditing(true);
				}
			}

			if (Auditing.hasTableAuditing(level)) {
				// NOTE: the no object mapping will exist if the object no
				// longer exists in the schema
				EntityMapping om = schema.findEntityMapping(entityName);

				if (om != null) {
					// Get the table
					Table table = om.getTable();

					// Create the audit table
					Table auditTable = null;
					try {
						auditTable = Auditing.getAuditingTable(table, Auditing.hasFieldAuditing(level));
					} catch (AuditingException e) {
						throw new SchemaTaskException(e);
					}

					// Add the audit table to the correct database in the schema
					schema.findDatabase(table.database.DatabaseName).Tables.add(auditTable);
				}
			}
		}
        return schema;
    }*/
}