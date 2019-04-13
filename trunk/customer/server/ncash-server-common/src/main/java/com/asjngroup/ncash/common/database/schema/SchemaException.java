package com.asjngroup.ncash.common.database.schema;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class SchemaException extends NCashException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public SchemaException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public SchemaException( Throwable t )
    {
        super( "", t );
    }

    public SchemaException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public SchemaException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }

}
