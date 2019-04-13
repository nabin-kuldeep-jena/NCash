package com.asjngroup.ncash.common.xml;

import com.asjngroup.ncash.common.exception.NCashException;


public class XMLTreeException extends NCashException
{
    public XMLTreeException( String str )
    {
        super( str );
    }

    public XMLTreeException( Throwable t )
    {
        super( t );
    }

    public XMLTreeException( String str, Object... args )
    {
        super( str, args );
    }

    public XMLTreeException( String str, Throwable t, Object... args )
    {
        super( str, t, args );
    }
}
