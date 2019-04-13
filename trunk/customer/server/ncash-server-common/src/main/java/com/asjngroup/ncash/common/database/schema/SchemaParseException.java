package com.asjngroup.ncash.common.database.schema;

import com.asjngroup.ncash.common.exception.NCashException;


public class SchemaParseException extends NCashException
{
    public SchemaParseException( String str )
    {
        super( str );
    }

    public SchemaParseException( Throwable t )
    {
        super( t );
    }

    public SchemaParseException( String str, Object... args )
    {
        super( str, args );
    }

    public SchemaParseException( String str, Throwable t, Object... args )
    {
        super( str, t, args );
    }
}
