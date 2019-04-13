package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.io.util.FileHelper;
import com.asjngroup.ncash.common.io.util.FileIOResourceHelper;
import com.asjngroup.ncash.common.io.util.InputHandler;

public class HibernateObjectCodeGenerator
{

	public static void main( String[] paramArrayOfString )
	{
		try
		{
			ArrayList extraImportPackages = getExtraImportPackages( paramArrayOfString );
			String objectGeneratePath = paramArrayOfString[paramArrayOfString.length-1];
			new HibernateObjectCodeGenerator().generate( extraImportPackages, objectGeneratePath );
		}
		catch ( Exception localException )
		{
			localException.printStackTrace();
			System.exit( 1 );
		}
	}

	private static ArrayList<String> getExtraImportPackages( String[] paramArrayOfString )
	{
		ArrayList localArrayList = new ArrayList();
		for ( String str : paramArrayOfString )
		{
			if ( ( !( str.split( ":" )[0].equals( "IMPORT_PACKAGE" ) ) ) || ( str.split( ":" ).length != 2 ) )
				continue;
			localArrayList.add( str.split( ":" )[1].replace( " ", "" ) );
		}
		return localArrayList;
	}

	protected void generate( ArrayList<String> extraImportPackages, String objectGeneratePath ) throws Exception
	{
		final List<SchemaTag> allSchemas = new ArrayList<SchemaTag>();
		final Map<SchemaTag, File> generateSchemas = new HashMap<SchemaTag, File>();
		final List<String> auditList = new ArrayList<String>();

		List<String> metaDatas = Metadata.getAllMetadataFiles();
		FileIOResourceHelper.processResources( metaDatas, new InputHandler<URL, InputStream>()
		{
			public void process( URL source, InputStream stream ) throws Exception
			{
				Metadata metadata = Metadata.loadMetadata( stream );

				if ( metadata.getDatabaseRootSchema() == null )
					return;

				/*Auditing auditing = metadata.getAuditing();
				if (auditing != null) {
					List<AuditLevel> aulList = auditing.getAuditLevelRoot(source);
					if (aulList != null && !aulList.isEmpty()) {
						auditList.addAll(getAuditEntities(aulList));
					}
				}
				*/
				if ( FileIOResourceHelper.isResourceInArchive( source ) )
				{
					allSchemas.add( metadata.getDatabaseRootSchema().getLatestSchema().getSchema() );
				}
				else
				{
					// establish the source directory and check it is valid
					File srcDirectory = getSourceDirectory( source, "src/main/resources" );
					checkDirectory( srcDirectory );

					// find the latest schema, can't use the standard classpath
					// scanning as the code is not compiled yet!
					SchemaTag currentLatestSchema = null;

					for ( DatabaseSchema modelSchema : metadata.getDatabaseRootSchema().getDatabaseSchemas() )
					{
						File file = new File( srcDirectory, modelSchema.getPath() );

						Serializer serializer = new Persister();
						SchemaTag schema = serializer.read( SchemaTag.class, file );

						if ( currentLatestSchema == null )
						{
							currentLatestSchema = schema;
						}
						else
						{
							int result = currentLatestSchema.compareTo( schema );

							if ( result == 0 )
								throw new NCashRuntimeException( "Two schemas with identical versions '%1'", schema.toString() );

							if ( result < 0 )
							{
								currentLatestSchema = schema;
								generateSchemas.put( currentLatestSchema, srcDirectory );
								allSchemas.add( currentLatestSchema );
							}
						}
					}

					if ( metadata.getDatabaseRootSchema().getDatabaseSchemas().size() == 1 && currentLatestSchema != null )
					{
						generateSchemas.put( currentLatestSchema, srcDirectory );
						allSchemas.add( currentLatestSchema );
					}

					if ( currentLatestSchema == null )
						throw new NCashRuntimeException( "No schemas found in package '%1'", metadata.getName() );

				}
			}
		} );

		// Picks the latest schema file for generation among 'n' products
		// specified.
		Map<String, List<Object>> uniqueSchemas = new HashMap<String, List<Object>>();
		for ( Map.Entry<SchemaTag, File> entry : generateSchemas.entrySet() )
		{
			List<Object> entries = new ArrayList<Object>();
			entries.add( entry.getKey() );
			entries.add( entry.getValue() );
			if ( uniqueSchemas.get( entry.getKey().getProduct() ) != null )
			{
				SchemaTag schemaInMap = ( SchemaTag ) uniqueSchemas.get( entry.getKey().getProduct() ).get( 0 );
				if ( isNewerVersion( entry.getKey(), schemaInMap ) )
				{
					uniqueSchemas.put( entry.getKey().getProduct(), entries );
				}
				else
					continue;
			}
			else
			{
				uniqueSchemas.put( entry.getKey().getProduct(), entries );
			}
		}

		// process
		for ( Map.Entry<String, List<Object>> entry : uniqueSchemas.entrySet() )
		{

			processFile( allSchemas, auditList, ( SchemaTag ) entry.getValue().get( 0 ), new File( objectGeneratePath ), extraImportPackages );
		}
	}

	/*private Collection< ? extends String> getAuditEntities( List<AuditLevel> paramList )
	{
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = paramList.iterator();
		while ( localIterator.hasNext() )
		{
			AuditLevel localAuditLevel = ( AuditLevel ) localIterator.next();
			localArrayList.add( localAuditLevel.getEntEntity() );
		}
		return localArrayList;
	}*/

	private boolean isNewerVersion( SchemaTag paramSchemaTag1, SchemaTag paramSchemaTag2 )
	{
		int i = paramSchemaTag1.getMajorVersion() - paramSchemaTag2.getMajorVersion();
		if ( i >= 0 )
		{
			int j = paramSchemaTag1.getMinorVersion() - paramSchemaTag2.getMinorVersion();
			if ( j >= 0 )
				return true;
		}
		return false;
	}

	private void processFile( List<SchemaTag> paramList, List<String> paramList1, SchemaTag paramSchemaTag, File paramFile, ArrayList<String> paramArrayList ) throws Exception
	{
		File localFile1 = new File( paramFile, paramSchemaTag.getServerPackage().replace( ".", FileHelper.fileSeperator ) + FileHelper.fileSeperator );
		if ( ( !( localFile1.exists() ) ) && ( !( localFile1.mkdirs() ) ) )
			throw new NCashRuntimeException( "Failed to create directory '%1'", new Object[]
			{ localFile1 } );
		Iterator localIterator = paramSchemaTag.getEntityMappings().iterator();
		while ( localIterator.hasNext() )
		{
			EntityMapping localEntityMapping = ( EntityMapping ) localIterator.next();
			JavaObjectBuilder localJavaObjectBuilder = new JavaObjectBuilder( paramList, paramSchemaTag, localEntityMapping );
			String str = localEntityMapping.getEntityName();
			PrintWriter localPrintWriter1 = new PrintWriter( new FileOutputStream( new File( localFile1, str + ".java" ) ) );
			localJavaObjectBuilder.generateImplementation( localPrintWriter1, paramArrayList );
			if ( paramList1.contains( str ) )
			{
				File localFile2 = new File( paramFile, paramSchemaTag.getServerPackage().replace( ".", FileHelper.fileSeperator ) + FileHelper.fileSeperator + "audit" + FileHelper.fileSeperator );
				if ( ( !( localFile2.exists() ) ) && ( !( localFile2.mkdirs() ) ) )
					throw new NCashRuntimeException( "Failed to create directory '%1'", new Object[]
					{ localFile2 } );
				PrintWriter localPrintWriter2 = new PrintWriter( new FileOutputStream( new File( localFile2, str + "Adt.java" )) );
				localJavaObjectBuilder.generateAuditImplementation( localPrintWriter2 );
				localPrintWriter2.close();
			}
			localPrintWriter1.close();
		}
	}

	protected void checkDirectory( File paramFile ) throws NCashRuntimeException
	{
		if ( !( paramFile.exists() ) )
			throw new NCashRuntimeException( "Directory '%1' does not exist", new Object[]
			{ paramFile.getAbsolutePath() } );
		if ( paramFile.isDirectory() )
			return;
		throw new NCashRuntimeException( "Directory '%1' is not a directory", new Object[]
		{ paramFile.getAbsolutePath() } );
	}

	public static boolean inBaseClass( EntityMapping paramEntityMapping, FieldMapping paramPropertyMapping )
	{
		if ( SchemaHelper.getIdPropertyName( paramEntityMapping.getEntityPrefix() ).equals( paramPropertyMapping.getFieldName() ) )
			return true;
		if ( SchemaHelper.getVersionIdPropertyName( paramEntityMapping.getEntityPrefix() ).equals( paramPropertyMapping.getFieldName() ) )
			return true;
		if ( SchemaHelper.getDeleteFlPropertyName( paramEntityMapping.getEntityPrefix() ).equals( paramPropertyMapping.getFieldName() ) )
			return true;
		if ( SchemaHelper.getSystemGeneratedFlPropertyName().equals( paramPropertyMapping.getFieldName() ) )
			return true;
		return ( ( SchemaHelper.getPtnIdPropertyName().equals( paramPropertyMapping.getFieldName() ) ) && ( !( paramEntityMapping.getEntityPrefix().equals( "Ptn" ) ) ) );
	}

	public static File getSourceDirectory( URL paramURL, String relativePath ) throws Exception
	{
		return new File( new File( paramURL.toURI() ).getParentFile().getParentFile().getParentFile().getParentFile(), relativePath );
	}
}