package com.asjngroup.ncash.installer.steps;

import com.asjngroup.ncash.installer.tasks.schema.UpdateSchemaTask;

public class InstallerStartUp
{
	public static void main( String[] args )
	{
		UpdateSchemaTask.start();
	}
}
