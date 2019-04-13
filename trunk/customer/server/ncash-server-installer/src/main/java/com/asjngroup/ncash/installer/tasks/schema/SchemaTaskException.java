package com.asjngroup.ncash.installer.tasks.schema;

import com.asjngroup.ncash.common.util.StringUtil;
import com.asjngroup.ncash.installer.tasks.InstallerTaskException;

public class SchemaTaskException extends InstallerTaskException
{

	private static final long serialVersionUID = 1L;

	public SchemaTaskException( String str )
    {
        super( StringUtil.create( str ) );
    }

	public SchemaTaskException( Throwable t )
    {
        super( "", t );
    }

	public SchemaTaskException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

	public SchemaTaskException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
