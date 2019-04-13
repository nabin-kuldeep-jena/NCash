package com.asjngroup.ncash.installer.tasks;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringUtil;

public class InstallerTaskException extends NCashRuntimeException
{
	private static final long serialVersionUID = 1L;

	public InstallerTaskException( String str )
    {
        super( StringUtil.create( str ) );
    }

	public InstallerTaskException( Throwable t )
    {
        super( "", t );
    }

	public InstallerTaskException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

	public InstallerTaskException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }

}
