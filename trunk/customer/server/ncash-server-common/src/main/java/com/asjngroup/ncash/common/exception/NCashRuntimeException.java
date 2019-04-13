package com.asjngroup.ncash.common.exception;

import com.asjngroup.ncash.common.util.StringUtil;

public class NCashRuntimeException extends RuntimeException 
{

	private static final long serialVersionUID = 1L;

	public NCashRuntimeException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public NCashRuntimeException( Throwable t )
    {
        super( "", t );
    }

    public NCashRuntimeException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public NCashRuntimeException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
