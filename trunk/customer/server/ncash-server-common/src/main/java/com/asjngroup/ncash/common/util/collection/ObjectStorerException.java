package com.asjngroup.ncash.common.util.collection;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class ObjectStorerException extends NCashRuntimeException
{
    public ObjectStorerException( String str )
    {
        super( str );
    }

    public ObjectStorerException( Throwable t )
    {
        super( t );
    }

    public ObjectStorerException( String str, Object... args )
    {
        super( str, args );
    }

    public ObjectStorerException( String str, Throwable t, Object... args )
    {
        super( str, t, args );
    }
}
