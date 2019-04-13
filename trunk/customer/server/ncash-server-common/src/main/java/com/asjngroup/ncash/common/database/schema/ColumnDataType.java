package com.asjngroup.ncash.common.database.schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;

public enum ColumnDataType
{
    Long, Int, String, DateTime, Bool, Decimal, Text, Unknown, Boolean;

    public int toJDBCType()
    {
        switch (this)
        {
        case Long:
            return Types.BIGINT;

        case Int:
            return Types.INTEGER;

        case String:
            return Types.VARCHAR;

        case DateTime:
            return Types.TIMESTAMP;

        case Bool:
            return Types.CHAR;

        case Decimal:
            return Types.BIGINT;

        case Text:
            return Types.LONGVARCHAR;
            
        case Boolean:
        	return Types.BOOLEAN;

        default:
            throw new IllegalStateException( StringUtil.create( "Can't convert spark type '%1' to jdbc type", this ) );

        }
    }

    public static ColumnDataType typeof( Object obj )
    {
        if ( obj == null )
            return ColumnDataType.Unknown;

        if ( obj instanceof String )
            return ColumnDataType.String;

        if ( obj instanceof org.joda.time.DateTime )
            return ColumnDataType.DateTime;

        if ( obj instanceof Boolean )
            return ColumnDataType.Bool;

        if ( obj instanceof Long )
            return ColumnDataType.Long;

        if ( obj instanceof Integer )
            return ColumnDataType.Int;

        if ( obj instanceof BigDecimal )
            return ColumnDataType.Decimal;

        return ColumnDataType.Unknown;
    }

    public static ColumnDataType typeofString( String type )
    {
        return ColumnDataTypeHelper.typeStringToColumnDataType( type );
    }

    public static ColumnDataType toSparkTypeFromTableColumnType( String tclType )
    {
        if(tclType.equals("bool") || tclType.equals("boolean"))
            return ColumnDataType.Bool ;

        if(tclType.equals("decimal"))
            return ColumnDataType.Decimal ;

        if(tclType.equals("long"))
            return ColumnDataType.Long ;

        if(tclType.equals("datetime"))
            return ColumnDataType.DateTime ;

        if(tclType.equals("string"))
            return ColumnDataType.String ;

        if(tclType.equals("int") || tclType.equals("integer"))
            return ColumnDataType.Int ;

        return ColumnDataType.Unknown ;

    }

    // IMPORTANT: jdbc types do not map 1->1 with spark types so this is approximation
    // should only be used in certain circumstances as information may be lost if the
    // result type is used to extract column information
    public static ColumnDataType jdbcTypeToCompatibleColumnDataType( ResultSet resultSet, int columnIndex ) throws SQLException
    {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType( columnIndex ))
        {
        case Types.NUMERIC:
        {
            int precision = metaData.getPrecision( columnIndex );

            if ( precision <= 10 )
                return ColumnDataType.Int;

            return ColumnDataType.Long;
        }

        case Types.BIGINT:
            return ColumnDataType.Long;

        case Types.INTEGER:
            return ColumnDataType.Int;

        case Types.TIMESTAMP:
        case Types.DATE:
            return ColumnDataType.DateTime;

        case Types.VARCHAR:
        case Types.NVARCHAR:
            return ColumnDataType.String;

        case Types.CHAR:
            return ColumnDataType.Bool;

        default:
            return ColumnDataType.Unknown;
        }
    }

    public Column toColumn( Table table, String name, boolean useMaxLengthForString )
    {
        if ( this == ColumnDataType.String )
        {
            return new Column( name, this, useMaxLengthForString ? 4000 : 255, false, table );
        }
        else
        {
            return new Column( name, this, false, table );
        }
    }

    public Column toColumn( Table table, String name )
    {
        if ( this == ColumnDataType.String )
        {
            return new Column( name, this, 255, false, table );
        }
        else
        {
            return new Column( name, this, false, table );
        }
    }

    public int getLength()
    {
        switch (this)
        {
        case String:
            return 255;

        case Long:
        case Int:
        case DateTime:
        case Bool:
        case Decimal:
        case Text:
            return 0;

        default:
            throw new IllegalStateException( StringUtil.create( "Can't convert spark type '%1' to jdbc type", this ) );

        }
    }

    public String toTableColumnType()
    {
        // cheesy but does the job
        return toString().toLowerCase();
    }
    
	public Object getValue( String value, int scaleValue )
	{
		if ( StringHelper.isEmpty( value ) )
			return null;
		switch( this )
		{
		case String:
			return value;
		case Long:
			return java.lang.Long.parseLong( value );
		case Int:
			return Integer.parseInt( value );
		case DateTime:
			try
			{
				//Default parse output format is 'yyyyMMdd HH:mm:ss'
				return new org.joda.time.DateTime( new SimpleDateFormat( "yyyyMMdd HH:mm:ss" ).parse( value ).getTime() );
			}
			catch ( ParseException e )
			{
				throw new IllegalStateException( StringUtil.create( "Can't convert spark value '%1' to date type", value ) );
			}
		case Bool:
			return "Y".equalsIgnoreCase( value );
		case Decimal:
			return new BigDecimal( value ).setScale( scaleValue ).divide( new BigDecimal( Math.pow( 10, scaleValue ) ), RoundingMode.HALF_EVEN );
		case Text:
			return value;

		default:
			throw new IllegalStateException( StringUtil.create( "Can't convert spark value '%1' to spark type", value ) );
		}
	}
}
