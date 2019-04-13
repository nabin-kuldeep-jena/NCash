package com.asjngroup.ncash.installer.tasks;

import java.util.HashMap;

public class InstallerData
{
	private HashMap<String, Object> data = new HashMap<String, Object>();

	public Object getData( String key )
	{
		return data.get( key );
	}

	public void setData( String key, String value )
	{
		data.put( key, value );
	}

}
