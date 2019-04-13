package com.asjngroup.ncash.common.properties;

public class NCashServerProperties
{
	private boolean isDevMode;

	public static boolean isDevMode()
	{
		return Boolean.TRUE.equals( System.getenv( "ncash.serverproperty.isDevMode" ) );
	}

}
