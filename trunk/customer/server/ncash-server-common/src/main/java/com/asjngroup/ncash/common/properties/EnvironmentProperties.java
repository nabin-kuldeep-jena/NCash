package com.asjngroup.ncash.common.properties;

public class EnvironmentProperties
{

	private static NCashSystemPropertes nCashSystemPropertes=new NCashSystemPropertes();
	
	private static NCashServerProperties nCashServerProperties=new NCashServerProperties();

	public static NCashSystemPropertes getNcashSystemPropertes()
	{
		return nCashSystemPropertes;
	}

	public static NCashServerProperties getNcashServerProperties()
	{
		return nCashServerProperties;
	}
}
