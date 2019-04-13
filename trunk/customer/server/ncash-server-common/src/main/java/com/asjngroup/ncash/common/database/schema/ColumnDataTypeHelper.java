package com.asjngroup.ncash.common.database.schema;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;


public class ColumnDataTypeHelper
{
    public static Class columnDataTypeToResultSetClass( ColumnDataType columnDataType )
    {
        switch (columnDataType)
        {
        case Long:
            return Long.class;
        case Int:
            return Integer.class;
        case String:
            return String.class;
        case DateTime:
            return DateTime.class;
        case Bool:
            return Boolean.class;
        case Decimal:
            return BigDecimal.class;
        case Text:
            return String.class;
        default:
            throw new IllegalArgumentException( "Invalid column type passed " + columnDataType );
        }
    }

    public static Class columnDataTypeToFunctionClass( ColumnDataType columnDataType )
    {
        switch (columnDataType)
        {
        case Long:
            return Long.class;
        case Int:
            return Integer.class;
        case String:
            return String.class;
        case DateTime:
            return DateTime.class;
        case Bool:
            return Boolean.class;
        case Decimal:
            return BigDecimal.class;
        case Text:
            return String.class;
        default:
            throw new IllegalArgumentException( "Invalid column type passed " + columnDataType );
        }
    }

    public static Class typeStringToResultSetClass( String type )
    {
        if ( type.equals( SchemaConstant.DT_LONG ) )
            return Long.class;
        if ( type.equals( SchemaConstant.DT_INT ) )
            return Integer.class;
        if ( type.equals( SchemaConstant.DT_STRING ) )
            return String.class;
        if ( type.equals( SchemaConstant.DT_DATE ) )
            return DateTime.class;
        if ( type.equals( SchemaConstant.DT_DATETIME ) )
            return DateTime.class;
        if ( type.equals( SchemaConstant.DT_BOOL ) )
            return String.class;
        if ( type.equals( SchemaConstant.DT_DECIMAL ) )
            return Long.class;

        throw new IllegalArgumentException( "Invalid type string passed " + type );
    }

    public static ColumnDataType fieldClassToColumnDataType( Class clazz )
    {
        if ( clazz.equals( Integer.class ) || clazz.equals( Integer.TYPE ) )
            return ColumnDataType.Int;

        if ( clazz.equals( String.class ) )
            return ColumnDataType.String;

        if ( clazz.equals( DateTime.class ) )
            return ColumnDataType.DateTime;

        if ( clazz.equals( Boolean.TYPE ) )
            return ColumnDataType.Bool;

        if ( clazz.equals( Long.class ) || clazz.equals( Long.TYPE ) )
            return ColumnDataType.Long;

        if ( clazz.equals( BigDecimal.class ) )
            return ColumnDataType.Decimal;

        throw new IllegalArgumentException( "Invalid class passed " + clazz.getName() );
    }

    public static ColumnDataType typeStringToColumnDataType( String type )
    {
        if ( type.equalsIgnoreCase( SchemaConstant.DT_LONG ) )
            return ColumnDataType.Long;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_INT ) || type.equalsIgnoreCase( SchemaConstant.DT_INTEGER ))
            return ColumnDataType.Int;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_STRING ) )
            return ColumnDataType.String;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_DATE ) )
            return ColumnDataType.DateTime;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_DATETIME ) )
            return ColumnDataType.DateTime;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_BOOL ) || type.equalsIgnoreCase( SchemaConstant.DT_BOOLEAN ) )
            return ColumnDataType.Bool;
        if ( type.equalsIgnoreCase( SchemaConstant.DT_DECIMAL ) )
            return ColumnDataType.Decimal;

        throw new IllegalArgumentException( "Invalid type string passed " + type );
    }
    
    public static ColumnDataType[] formColumnDataTypeFromColumns(List<Column> columnList) 
    {
        if (columnList.isEmpty())
            return new ColumnDataType[] {};

        ColumnDataType[] columnDataTypes = new ColumnDataType[columnList.size()];

        for (int index = 0; index < columnList.size(); index++) {
            columnDataTypes[index] = columnList.get(index).getDataType();
        }

        return columnDataTypes;
    }
    
	public static boolean isNumericColumnDataType( Object type )
	{
		if ( type instanceof Long || type instanceof Integer || type instanceof BigDecimal )
			return true;
		return false;
	}
	
	public static boolean isDateTimeColumnDataType( String type )
	{
		if ( type.equalsIgnoreCase( SchemaConstant.DT_DATE ) || type.equalsIgnoreCase( SchemaConstant.DT_DATETIME ) )
			return true;
		return false;
	}
}
