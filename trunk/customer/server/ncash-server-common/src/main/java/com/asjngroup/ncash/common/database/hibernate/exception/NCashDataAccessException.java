package com.asjngroup.ncash.common.database.hibernate.exception;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class NCashDataAccessException extends NCashException
{
	private static final long serialVersionUID = 1L;

	public NCashDataAccessException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public NCashDataAccessException( Throwable t )
    {
        super( "", t );
    }

    public NCashDataAccessException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public NCashDataAccessException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
