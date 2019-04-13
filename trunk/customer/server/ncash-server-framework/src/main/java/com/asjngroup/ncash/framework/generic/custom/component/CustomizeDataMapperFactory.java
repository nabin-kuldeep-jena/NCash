package com.asjngroup.ncash.framework.generic.custom.component;

import java.util.HashMap;
import java.util.Map;

public class CustomizeDataMapperFactory
{
	private static CustomizeDataMapperFactory INSTANCE;
	private Map<String, CustomizeDataMapper> customeDataMapper = new HashMap<>();
	private Map<String, String> customeDataMapperKey = new HashMap<>();

	public void setCustomeDataMapperKey(String ... value)
	{
		this.customeDataMapperKey.put( value[0], value[1] );
	}

	public CustomizeDataMapper getCustomDataMapper( String key )
	{
		return customeDataMapper.get( customeDataMapperKey.get( key ) );
	}

	public void setCustomDataMapper( CustomizeDataMapper customizeDataMapper )
	{
		customeDataMapper.put( customizeDataMapper.getClass().getSimpleName(), customizeDataMapper );
	}

	public static CustomizeDataMapperFactory getInstance()
	{
		if ( INSTANCE == null )
			INSTANCE = new CustomizeDataMapperFactory();
		return INSTANCE;
	}

}
