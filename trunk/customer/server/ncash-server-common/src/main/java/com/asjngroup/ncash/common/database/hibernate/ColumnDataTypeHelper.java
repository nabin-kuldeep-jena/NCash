package com.asjngroup.ncash.common.database.hibernate;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class ColumnDataTypeHelper
{
	public static ColumnDataType typeStringToColumnDataType( String paramString )
	{
		if ( paramString.equals( "long" ) )
			return ColumnDataType.Long;
		if ( paramString.equals( "int" ) )
			return ColumnDataType.Int;
		if ( paramString.equals( "string" ) )
			return ColumnDataType.String;
		if ( paramString.equals( "date" ) )
			return ColumnDataType.DateTime;
		if ( paramString.equals( "datetime" ) )
			return ColumnDataType.DateTime;
		if ( paramString.equals( "bool" ) )
			return ColumnDataType.Bool;
		if ( paramString.equals( "decimal" ) )
			return ColumnDataType.Decimal;
		if ( paramString.equals( "time" ) )
			return ColumnDataType.Time;
		if ( paramString.equals( "timespan" ) )
			return ColumnDataType.Timespan;
		throw new IllegalArgumentException( "Invalid type string passed " + paramString );
	}

	public static ColumnDataType getColumnDataTypeForWorkStepPropertyType( String paramString )
	{
		if ( "String Property".equals( paramString ) )
			return ColumnDataType.String;
		if ( "Flag Property".equals( paramString ) )
			return ColumnDataType.Bool;
		if ( "Date Property".equals( paramString ) )
			return ColumnDataType.DateTime;
		if ( "Integer Property".equals( paramString ) )
			return ColumnDataType.Int;
		if ( "Long Property".equals( paramString ) )
			return ColumnDataType.Long;
		if ( "Decimal Property".equals( paramString ) )
			return ColumnDataType.Decimal;
		if ( "Hard Lookup Property".equals( paramString ) )
			return ColumnDataType.String;
		if ( "SQL Lookup Property".equals( paramString ) )
			return ColumnDataType.String;
		throw new IllegalArgumentException( paramString + " is not recognized as valid datatype" );
	}

	public static ColumnDataType fieldClassToColumnDataType( Class< ? > paramClass )
	{
		if ( ( paramClass.equals( Integer.class ) ) || ( paramClass.equals( Integer.TYPE ) ) )
			return ColumnDataType.Int;
		if ( paramClass.equals( String.class ) )
			return ColumnDataType.String;
		if ( paramClass.equals( DateTime.class ) )
			return ColumnDataType.DateTime;
		if ( ( paramClass.equals( Boolean.TYPE ) ) || ( paramClass.equals( Boolean.class ) ) )
			return ColumnDataType.Bool;
		if ( ( paramClass.equals( Long.class ) ) || ( paramClass.equals( Long.TYPE ) ) )
			return ColumnDataType.Long;
		if ( paramClass.equals( BigDecimal.class ) )
			return ColumnDataType.Decimal;
		return null;
	}

	public static Class< ? > ColumnDataTypeToFunctionClass( ColumnDataType paramColumnDataType )
	{
		switch( paramColumnDataType.ordinal() )
		{
		case 1:
			return Long.class;
		case 2:
			return Integer.class;
		case 3:
			return String.class;
		case 4:
			return DateTime.class;
		case 5:
			return Boolean.class;
		case 6:
			return BigDecimal.class;
		case 7:
			return String.class;
		}
		throw new IllegalArgumentException( "Invalid ncash schema type passed " + paramColumnDataType );
	}
}
