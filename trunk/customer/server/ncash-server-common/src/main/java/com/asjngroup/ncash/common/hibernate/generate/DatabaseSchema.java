package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.InputStream;

import com.asjngroup.ncash.common.io.util.FileIOResourceHelper;

@Root( name = "databaseSchema" )
public class DatabaseSchema
{

	@Attribute( name = "path" )
	private String path;
	private SchemaTag schema;

	public String getPath()
	{
		return this.path;
	}

	public SchemaTag getSchema()
	{
		if ( this.schema == null )
		{
			InputStream localInputStream = FileIOResourceHelper.getResourceAsStream( getPath() );
			try
			{
				this.schema = SchemaTag.loadSchema( localInputStream );
			}
			finally
			{
				FileIOResourceHelper.closeSilent( localInputStream );
			}
		}
		return this.schema;
	}
}