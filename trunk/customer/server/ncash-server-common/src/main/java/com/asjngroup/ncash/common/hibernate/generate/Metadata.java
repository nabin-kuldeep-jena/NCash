package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.io.util.FileIOResourceHelper;
import com.asjngroup.ncash.common.io.util.InputHandler;
import com.asjngroup.ncash.common.util.StringHelper;

@Root( name = "metadata" )
public class Metadata
{

	@Attribute( name = "name" )
	private String name;

	@Attribute( name = "majorVersion" )
	private Integer majorVersion;

	@Attribute( name = "minorVersion" )
	private Integer minorVersion;

	@Attribute( name = "revisionVersion" )
	private Integer revisionVersion;

	@Attribute( name = "parent", required = false )
	private String parent;

	@Attribute( name = "optional", required = false )
	private boolean optional = false;

	@Element( name = "database", required = false )
	private DatabaseRootSchema databaseRootSchema;
	private static List<Metadata> metadatas = null;

	public static Metadata loadMetadata( InputStream paramInputStream )
	{
		try
		{
			Persister localPersister = new Persister();
			return ( ( Metadata ) localPersister.read( Metadata.class, paramInputStream ) );
		}
		catch ( Exception localException )
		{
			throw new NCashRuntimeException( localException );
		}
	}

	public static List<Metadata> getMetadataByInput( List<String> metaDatas )
	{
		if ( metadatas == null )
		{
			// load the list of metadata (unsorted)
			final List<Metadata> unsortedMetadatas = new ArrayList<Metadata>();
			FileIOResourceHelper.processResources( metaDatas, new InputHandler<URL, InputStream>()
			{
				public void process( URL source, InputStream stream ) throws Exception
				{
					unsortedMetadatas.add( Metadata.loadMetadata( stream ) );
				}
			} );

			// sort the metadata based on hierarchy
			List<Metadata> sortedMetadatas = new ArrayList<Metadata>();

			while ( !unsortedMetadatas.isEmpty() )
			{
				int startCount = unsortedMetadatas.size();

				for ( Iterator<Metadata> iterator = unsortedMetadatas.iterator(); iterator.hasNext(); )
				{
					Metadata unsortedMetadata = iterator.next();

					if ( StringHelper.isEmpty( unsortedMetadata.getParent() ) )
					{
						sortedMetadatas.add( unsortedMetadata );
						iterator.remove();
					}
					else
					{
						List<Metadata> temp = new ArrayList<Metadata>();

						for ( Metadata metadata : sortedMetadatas )
						{
							if ( unsortedMetadata.getParent().equals( metadata.getName() ) )
							{
								temp.add( unsortedMetadata );
								iterator.remove();
								break;
							}
						}

						sortedMetadatas.addAll( temp );
					}
				}

				if ( unsortedMetadatas.size() == startCount )
					throw new NCashRuntimeException( "Could not sort metadatas, possible circular or broken reference." );
			}

			metadatas = Collections.unmodifiableList( sortedMetadatas );
		}

		return metadatas;
	}

	public static List<Metadata> getAllMetadata()
	{
		List localList = getAllMetadataFiles();
		return getMetadataByInput( localList );
	}

	public String getName()
	{
		return this.name;
	}

	public Integer getMajorVersion()
	{
		return this.majorVersion;
	}

	public Integer getMinorVersion()
	{
		return this.minorVersion;
	}

	public Integer getRevisionVersion()
	{
		return this.revisionVersion;
	}

	public String getParent()
	{
		return this.parent;
	}

	public boolean isOptional()
	{
		return this.optional;
	}

	public DatabaseRootSchema getDatabaseRootSchema()
	{
		return this.databaseRootSchema;
	}

	public static List<String> getAllMetadataFiles()
	{
		ArrayList localArrayList = new ArrayList();
		localArrayList.add( "META-INF/metadata.xml" );
		return localArrayList;
	}

}