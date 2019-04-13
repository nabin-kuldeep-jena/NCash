package com.asjngroup.ncash.common.database.datasource;

import java.io.File;

public class FileObjectForExtDir
{
	private File file;
	private String extDirName;

	public FileObjectForExtDir( File file, String extDirName )
	{
		this.setFile( file );
		this.setExtDirName( extDirName );
	}

	public File getFile()
	{
		return file;
	}

	public void setFile( File file )
	{
		this.file = file;
	}

	public String getExtDirName()
	{
		return extDirName;
	}

	public void setExtDirName( String extDirName )
	{
		this.extDirName = extDirName;
	}
}
