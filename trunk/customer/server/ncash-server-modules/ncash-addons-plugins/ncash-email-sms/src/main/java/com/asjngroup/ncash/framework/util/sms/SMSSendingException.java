package com.asjngroup.ncash.framework.util.sms;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class SMSSendingException extends NCashException
{

	private static final long serialVersionUID = 1L;

	public SMSSendingException( String str )
    {
        super( StringUtil.create( str ) );
    }

	public SMSSendingException( Throwable t )
    {
        super( "", t );
    }

	public SMSSendingException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

	public SMSSendingException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }

}
