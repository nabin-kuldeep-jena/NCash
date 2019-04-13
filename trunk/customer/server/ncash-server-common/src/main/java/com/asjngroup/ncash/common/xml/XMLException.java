package com.asjngroup.ncash.common.xml;

import com.asjngroup.ncash.common.exception.NCashException;

public class XMLException extends NCashException
{
    public XMLException( String str )
    {
        super( str );
    }

    public XMLException( Throwable t )
    {
        super( t );
    }

    public XMLException( String str, Object... args )
    {
        super( str, args );
    }

    public XMLException( String str, Throwable t, Object... args )
    {
        super( str, t, args );
    }
}
