package com.asjngroup.ncash.common.database.datasource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import com.asjngroup.ncash.common.database.schema.ColumnDataType;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class ResultSetRowSource implements RowSource<Object[]>
{
	private ResultSet rs;
	private ColumnDataType[] resultTypes;

	public ResultSetRowSource( ResultSet rs, ColumnDataType[] resultTypes )
	{
		this.rs = rs;
		this.resultTypes = resultTypes;

		normaliseResultTypes();

		try
		{
			DataSourceHelper.validateResultSet( rs, this.resultTypes );
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e );
		}
	}

	private void normaliseResultTypes()
	{
		// if result types is null then get the correct count and fill it with Unknown
		try
		{
			if ( resultTypes == null )
			{
				ResultSetMetaData resultSetMetaData = rs.getMetaData();

				resultTypes = new ColumnDataType[resultSetMetaData.getColumnCount()];
				Arrays.fill( this.resultTypes, ColumnDataType.Unknown );
			}

			// for each ColumnDataType.Unknown guess the result type
			for ( int i = 0; i < resultTypes.length; i++ )
			{
				if ( resultTypes[i] == ColumnDataType.Unknown )
				{
					// NOTE: this function does not always get it right if there is more than one ColumnDataType for the
					// column's JDBC return type (like the Spark Long vs Decimal, they are both stored as jdbc BIGINT)
					resultTypes[i] = ColumnDataType.jdbcTypeToCompatibleColumnDataType( rs, i + 1 );
				}
			}
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e );
		}
	}

	public boolean next()
	{
		try
		{
			return rs.next();
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e );
		}
	}

	public Object[] get()
	{
		try
		{
			return DataSourceHelper.unwrapResultSetRow( rs, resultTypes );
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e );
		}
	}

	public void beforeFirst()
	{
		try
		{
			rs.beforeFirst();
		}
		catch ( SQLException e )
		{
			throw new NCashRuntimeException( e );
		}

	}
}