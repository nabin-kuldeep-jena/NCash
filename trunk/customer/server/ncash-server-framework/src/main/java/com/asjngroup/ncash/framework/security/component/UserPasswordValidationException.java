package com.asjngroup.ncash.framework.security.component;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class UserPasswordValidationException extends NCashException
{
	private static final long serialVersionUID = 1L;

	public UserPasswordValidationException( String str )
    {
        super( StringUtil.create( str ) );
    }

	public UserPasswordValidationException( Throwable t )
    {
        super( "", t );
    }

	public UserPasswordValidationException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

	public UserPasswordValidationException	( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }

}
