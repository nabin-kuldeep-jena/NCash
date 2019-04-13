package com.asjngroup.ncash.common.exception;

import com.asjngroup.ncash.common.util.StringUtil;

public class NCashException extends Exception 
{

	private static final long serialVersionUID = 1L;

	public NCashException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public NCashException( Throwable t )
    {
        super( "", t );
    }

    public NCashException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public NCashException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
