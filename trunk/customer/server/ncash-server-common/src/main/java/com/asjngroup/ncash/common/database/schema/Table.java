package com.asjngroup.ncash.common.database.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;

/**
 * User: nabin.jena
 * Date: 08-Apr-2017
 * Time: 17:11:55
 */
public class Table implements Cloneable
{
    public String TableName;
    public String TablePrefix;

    public List< Column > Columns = new ArrayList< Column >();
    public List< Index > Indexes = new ArrayList< Index >();
    
    public Map< Integer, String > TableColumnMap = new HashMap< Integer, String >();

    public Integer TbdId;
    public Integer PtnId;
    
    public transient Database database;
	public int TinId;

    public Table()
    {
    }


    public Table( String tableName )
    {
        TableName = tableName;
    }

    public Table(String tableName, String tablePrefix)
    {
        TableName = tableName;
        TablePrefix = tablePrefix;
    }

    public void addColumn( Column column )
    {
        Columns.add( column );
    }
    
    public void addIndex( Index index )
    {
        Indexes.add( index );
    }

    public Table clone() throws CloneNotSupportedException
    {
        // Do a bitwise clone to start with
        Table clone = (Table)super.clone();

        // Handle any special properties
        clone.database = database;

        // Clone the columns collection
        clone.Columns = new ArrayList< Column >();

        for ( Column column : Columns )
        {
            Column newColumn = (Column)column.clone();
            newColumn.table = clone;
            clone.Columns.add( newColumn );
        }

        // Clone the indexes collection
        clone.Indexes = new ArrayList< Index >();
        for ( Index index : Indexes )
        {
            Index newIndex = (Index)index.clone();
            newIndex.table = clone;
            clone.Indexes.add( newIndex );
        }

        clone.TinId = TinId;
        
        return clone;
    }

    // finds a column by column name
    public Column findColumn( String columnName )
    {
        for ( Column column : Columns )
        {
            if ( column.ColumnName.equalsIgnoreCase( columnName ) )
                return column;
        }

        return null;
    }

    // finds an index by name
    public Index findIndex( String indexName )
    {
        for ( Index index : Indexes )
        {
            if ( index.IndexName.equalsIgnoreCase( indexName ) )
                return index;
        }

        return null;
    }

    public Index getClusteredIndex()
    {
        for ( Index index : Indexes )
        {
            if ( index.IsClustered )
                return index;
        }

        return null;
    }

    public List< String > getColumnNames()
    {
        List< String > columnNameList = new ArrayList< String >();

        for ( Column column : Columns )
        {
            columnNameList.add( column.ColumnName );
        }

        return columnNameList;
    }

    public boolean equals( Object object )
    {
        // see if the table structure and name equal
        if ( !isTableStructureEqual( object ) )
            return false;

        // compare the indexes
        if ( !areIndexesEqual( object ) )
            return false;

        return true;
    }

    public boolean isTableStructureEqual( Object object )
    {
        if ( object == null )
            return false;

        if ( !( object instanceof Table ) )
            return false;

        // compare the table name and columns
        Table fromTable = (Table)object;

        if ( !fromTable.TableName.equalsIgnoreCase( TableName ) )
            return false;

        // check the number of columns match
        if ( Columns.size() != fromTable.Columns.size() )
            return false;

        // check for columns that exist here but not in the from object or are different
        for ( int i = 0; i < Columns.size(); i++ )
        {
            // extract out the columns
            // NOTE: we use the index so we don't depend on the SequenceNumber starting from 0 or even sequential
            Column column = Columns.get( i );
            Column fromColumn = fromTable.Columns.get( i );

            // compare columns
            if ( !column.equals( fromColumn ) )
                return false;
        }

        // check for columns that exist in the from but not here
        for ( Column column : fromTable.Columns )
        {
            Column toColumn = findColumn( column.ColumnName );

            if ( toColumn == null )
                return false;
        }

        return true;
    }

    public boolean areIndexesEqual( Object object )
    {
        if ( object == null )
            return false;

        if ( !( object instanceof Table ) )
            return false;

        Table fromTable = (Table)object;

        if ( !fromTable.TableName.equalsIgnoreCase( TableName ) )
            return false;

        // check for indexes that exist here but not in the from object or are
        // different
        for ( Index index : Indexes )
        {
            Index fromIndex = fromTable.findIndex( index.IndexName );

            if ( fromIndex == null )
                return false;

            // compare columns
            if ( !index.equals( fromIndex ) )
                return false;
        }

        // check for indexes that exist in the from but not here
        for ( Index index : fromTable.Indexes )
        {
            Index toIndex = findIndex( index.IndexName );

            if ( toIndex == null )
                return false;
        }

        return true;
    }

    public Column getPrimaryKeyColumn()
    {
        String tablePrefix = getTablePrefix();

        if ( tablePrefix == null )
            throw new NCashRuntimeException( "Table prefix for %1 is null. Unable to determine the primary key column.", getTableName() );

        String idColumnName = StringHelper.objectNameToDatabaseName( StringHelper.getIdPropertyName( getTablePrefix() ) );
        for ( Column column : Columns )
        {
            if ( column.getColumnName().equals( idColumnName ) )
                return column;
        }

        return null;
    }

    public void setFieldAuditing( boolean fieldAuditing )
    {
        if ( fieldAuditing )
        {
            if ( findColumn( "modified_dttm" ) == null )
            {
                Columns.add( new Column( "modified_dttm", ColumnDataType.DateTime, false, this, true ) );
                Columns.add( new Column( "created_dttm", ColumnDataType.DateTime, false, this, true ) );
                Columns.add( new Column( "modified_usr_id", ColumnDataType.Int, false, this, true ) );
                Columns.add( new Column( "created_usr_id", ColumnDataType.Int, false, this, true ) );
            }
        }
        else
        {
            Column col = findColumn( "modified_dttm" );
            if ( col != null )
            {
                Columns.remove( col );
                Columns.remove( findColumn( "created_dttm" ) );
                Columns.remove( findColumn( "modified_usr_id" ) );
                Columns.remove( findColumn( "created_usr_id" ) );
            }
        }
    }

    public String getTableName()
    {
        return TableName;
    }

    public List<Index> getIndexes()
    {
        return Indexes;
    }

    public List<Column> getColumns()
    {
        return Columns;
    }

    public String getTablePrefix()
    {
        return TablePrefix;
    }

    public ColumnDataType[] getColumnTypes()
    {
        List< ColumnDataType > columnTypeList = new ArrayList<ColumnDataType>();

        for ( Column column : Columns )
        {
            columnTypeList.add( column.getDataType() );
        }

        return columnTypeList.toArray( new ColumnDataType[ 0 ] );
    }


	public Index findOlderDataIndex(String indexName) {
		
		for ( Index index : Indexes )
        {
            if ( index.IndexName.equalsIgnoreCase( indexName )  && index.IsOlderData)
                return index;
        }
		
		return null;
	}
	
	public Index findRecentDataIndex(String indexName) {
		
		for ( Index index : Indexes )
        {
            if ( index.IndexName.equalsIgnoreCase( indexName )  && index.IsRecentData)
                return index;
        }
		
		return null;
	}
}
