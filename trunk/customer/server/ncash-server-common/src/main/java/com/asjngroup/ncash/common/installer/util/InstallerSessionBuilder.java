package com.asjngroup.ncash.common.installer.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InstallerSessionBuilder
{
	public static ApplicationContext load()
	{
		ApplicationContext context = new ClassPathXmlApplicationContext( new String[]
		{ "ncash-common.xml", "ncash-common-datasource.xml" } );
		return context;
	}

}
