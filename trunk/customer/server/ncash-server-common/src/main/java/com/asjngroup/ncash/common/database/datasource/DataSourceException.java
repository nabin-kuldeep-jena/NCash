package com.asjngroup.ncash.common.database.datasource;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class DataSourceException extends NCashException
{

	private static final long serialVersionUID = 1L;

	public DataSourceException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public DataSourceException( Throwable t )
    {
        super( "", t );
    }

    public DataSourceException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public DataSourceException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
