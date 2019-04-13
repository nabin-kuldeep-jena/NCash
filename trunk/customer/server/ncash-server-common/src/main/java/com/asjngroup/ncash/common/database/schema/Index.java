package com.asjngroup.ncash.common.database.schema;


import java.io.IOException;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 10:18:11
 */
public class Index implements Cloneable
{
    public String IndexName;
    public boolean IsUnique;
    public boolean IsClustered;
    public boolean IsBusinessConstraint;
    public boolean IsIgnoreDupKey;
    
    public Integer TixId;

    // LOW: maybe change this to a list of string??
    public String ColumnList;

    public transient Table table;
	public boolean IsOlderData;
	public boolean IsRecentData;

    public Index()
    {
    }

    public Index( String indexName, boolean isUnique, boolean isClustered, boolean isBusinessConstraint, boolean isIgnoreDupKey, Table table )
    {
        IndexName = indexName;
        IsUnique = isUnique;
        IsClustered = isClustered;
        IsBusinessConstraint = isBusinessConstraint;
        IsIgnoreDupKey = isIgnoreDupKey;
        this.table = table;
    }

    public Index clone() throws CloneNotSupportedException
    {
        // Do a bitwise clone to start with
        Index clone = (Index)super.clone();

        // Handle any special properties
        clone.table = null;

        return clone;
    }

    public void addColumn( String columnName )
    {
        if ( ColumnList == null || ColumnList.length() == 0 )
        {
            ColumnList = columnName;
            return;
        }

        ColumnList = ColumnList + "," + columnName;
    }

    public boolean isIndexColumn(String colName)
    {
        String[] indexColumns=ColumnList.split ( ",");
        int i;
        for(i=0;i<indexColumns.length-1;i++);
        {
            if(colName.equalsIgnoreCase ( indexColumns[i]))
            {
                 return true;
            }
        }

       return false;
    }

    public String getIndexName()
    {
        return IndexName;
    }

    public boolean equals( Object object )
    {
        if ( object == null )
            return false;

        if ( !( object instanceof Index ) )
            return false;

        Index fromIndex = (Index)object;

        if ( !IndexName.equalsIgnoreCase( fromIndex.IndexName ) )
            return false;
        if ( IsUnique != fromIndex.IsUnique )
            return false;
        if ( IsClustered != fromIndex.IsClustered )
            return false;
        // NOTE: We do not test IsBusinessContraint as db schema cannot report this property
        if ( IsIgnoreDupKey != fromIndex.IsIgnoreDupKey )
            return false;

        if ( !ColumnList.equalsIgnoreCase( fromIndex.ColumnList ) )
            return false;

        return true;
    }
}
