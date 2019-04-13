package com.asjngroup.ncash.common.database.schema;


import java.io.IOException;

/**
 * User: nabin.jena
 * Date: 09-Apr-2017
 * Time: 10:11:09
 */
public class Column implements Cloneable
{
    public String ColumnName;
    public int SequenceNumber;
    public ColumnDataType DataType;
    public int Length;
    public boolean IsMandatory;
    public boolean IsPrimaryKey;
    public boolean isAuditEnabled;
    
    private String prefix = "";
    private String displayColumnName;
    private boolean hasValue = true;
   
    public boolean hasValue()
	{
		return hasValue;
	}

	public void setHasValue( boolean hasValue )
	{
		this.hasValue = hasValue;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix( String prefix )
	{
		this.prefix = prefix;
	}

	public String getDisplayColumnName()
	{
		return displayColumnName;
	}

	public void setDisplayColumnName( String displayColumnName )
	{
		this.displayColumnName = displayColumnName;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue( Object value )
	{
		this.value = value;
	}

	private Object value;

    public transient Table table;

    public Column()
    {
    }

    public Column( String columnName, ColumnDataType dataType, boolean isMandatory, Table table )
    {
        ColumnName = columnName;
        DataType = dataType;
        IsMandatory = isMandatory;
        this.table = table;
    }

    public Column( String columnName, ColumnDataType dataType, boolean isMandatory, Table table, boolean isAuditEnabled )
    {
        ColumnName = columnName;
        DataType = dataType;
        IsMandatory = isMandatory;
        this.isAuditEnabled = isAuditEnabled;
        this.table = table;
    }

    public Column( String columnName, ColumnDataType dataType, int length, boolean isMandatory, Table table )
    {
        ColumnName = columnName;
        DataType = dataType;
        Length = length;
        IsMandatory = isMandatory;
        this.table = table;
    }

    public Column( String columnName, ColumnDataType dataType, boolean isMandatory, int orderNo, boolean primaryKey, Table table )
    {
        ColumnName = columnName;
        DataType = dataType;
        IsMandatory = isMandatory;
        Length = dataType.getLength();
        SequenceNumber = orderNo;
        IsPrimaryKey = primaryKey;
        this.table = table;
    }

    public Column( String columnName, ColumnDataType dataType, int length, boolean isMandatory, int orderNo, boolean primaryKey, Table table )
    {
        ColumnName = columnName;
        DataType = dataType;
        Length = length;
        IsMandatory = isMandatory;
        SequenceNumber = orderNo;
        IsPrimaryKey = primaryKey;
        this.table = table;
    }

    public ColumnDataType getDataType()
    {
        return DataType;
    }

    public Column clone() throws CloneNotSupportedException
    {
        // Do a bitwise clone to start with
        Column clone = (Column)super.clone();

        // Handle any special properties
        clone.table = null;

        return clone;
    }

    public String getColumnName()
    {
        return ColumnName;
    }

    public boolean equals( Object object )
    {
        // NOTE: the sequence number check is implied by the order the Table compares its columns
        if ( object == null )
            return false;

        if ( !( object instanceof Column ) )
            return false;

        Column fromColumn = (Column)object;

        if ( !ColumnName.equalsIgnoreCase( fromColumn.ColumnName ) )
            return false;

        if ( !DataType.equals( fromColumn.DataType ) )
        {
            if ( !( ( DataType == ColumnDataType.Decimal || DataType == ColumnDataType.Long ) && ( fromColumn.DataType == ColumnDataType.Decimal || fromColumn.DataType == ColumnDataType.Long ) ) )
            {
                return false;
            }
        }

        // compares lengths for string only
        if ( DataType == ColumnDataType.String )
        {
            if ( Length != fromColumn.Length )
                return false;
        }

        if ( IsMandatory != fromColumn.IsMandatory )
            return false;

        return true;
    }
}
