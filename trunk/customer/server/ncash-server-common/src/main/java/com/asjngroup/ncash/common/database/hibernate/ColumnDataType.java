package com.asjngroup.ncash.common.database.hibernate;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.asjngroup.ncash.common.util.StringUtil;

public enum ColumnDataType
{
	Long, Int, String, DateTime, Bool, Decimal, Text, Time, Timespan, LongString, Unknown;

	public int toJDBCType()
  {
    switch (ordinal())
    {
    case 1:
    case 2:
      return -5;
    case 3:
      return 4;
    case 4:
    case 5:
      return 12;
    case 6:
      return 93;
    case 7:
      return 1;
    case 8:
      return -5;
    case 9:
      return -1;
    case 10:
      return 92;
    }
    throw new IllegalStateException(StringUtil.create("Can't convert schema type '%1' to jdbc type", new Object[] { this }));
  }

	public static ColumnDataType typeof( Object paramObject )
	{
		if ( paramObject == null )
			return Unknown;
		if ( paramObject instanceof String )
			return String;
		if ( paramObject instanceof DateTime )
			return DateTime;
		if ( paramObject instanceof Boolean )
			return Bool;
		if ( paramObject instanceof Long )
			return Long;
		if ( paramObject instanceof Integer )
			return Int;
		if ( paramObject instanceof BigDecimal )
			return Decimal;
		return Unknown;
	}

	public static ColumnDataType toNCashTypeFromTableColumnType( String paramString )
	{
		if ( ( paramString.equals( "bool" ) ) || ( paramString.equals( "boolean" ) ) )
			return Bool;
		if ( paramString.equals( "decimal" ) )
			return Decimal;
		if ( paramString.equals( "long" ) )
			return Long;
		if ( paramString.equals( "datetime" ) )
			return DateTime;
		if ( paramString.equals( "string" ) )
			return String;
		if ( ( paramString.equals( "int" ) ) || ( paramString.equals( "integer" ) ) )
			return Int;
		return Unknown;
	}

	public static ColumnDataType jdbcTypeToCompatibleNCash( ResultSet paramResultSet, int paramInt ) throws SQLException
	{
		ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
		switch( localResultSetMetaData.getColumnType( paramInt ) )
		{
		case 2:
			return ( ( localResultSetMetaData.getPrecision( paramInt ) <= 10 ) ? Int : Long );
		case -5:
			return Long;
		case 4:
			return Int;
		case 91:
		case 93:
			return DateTime;
		case -9:
		case 12:
			return String;
		case 1:
			return Bool;
		}
		return Unknown;
	}

	public int getLength()
  {
    switch (ordinal())
    {
    case 4:
      return 255;
    case 1:
    case 2:
    case 3:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
      return 0;
    }
    throw new IllegalStateException(StringUtil.create("Can't convert schema type '%1' to jdbc type", new Object[] { this }));
  }

	public String toTableColumnType()
	{
		return toString().toLowerCase();
	}

	public static ColumnDataType typeofString( String paramString )
	{
		return ColumnDataTypeHelper.typeStringToColumnDataType( paramString );
	}
}