package com.asjngroup.ncash.common.database.hibernate.exception;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class HibernateUtilException extends NCashException
{
	private static final long serialVersionUID = 1L;

	public HibernateUtilException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public HibernateUtilException( Throwable t )
    {
        super( "", t );
    }

    public HibernateUtilException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public HibernateUtilException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
