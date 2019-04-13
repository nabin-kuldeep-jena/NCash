package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class DatabaseRootSchema
{

	@ElementList( name = "databaseSchemas", type = DatabaseSchema.class )
	private List<DatabaseSchema> databaseSchemas;

	public List<DatabaseSchema> getDatabaseSchemas()
	{
		return this.databaseSchemas;
	}

	public List<DatabaseSchema> getSortedSchemas()
	{
		List<SchemaTag> schemas = new ArrayList<SchemaTag>();

		for ( DatabaseSchema databaseSchema : getDatabaseSchemas() )
		{
			SchemaTag schema = databaseSchema.getSchema();

			if ( !schemas.isEmpty() )
			{
				if ( !schemas.get( 0 ).getName().equals( schema.getName() ) )
					throw new NCashRuntimeException( "All schemas for the same database must match. Mismatch between '%1' and '%2'", schemas.get( 0 ).getName(), schema.getName() );
			}
			schemas.add( schema );
		}

		Collections.sort( schemas );

		List<DatabaseSchema> dbSchemas = new ArrayList<DatabaseSchema>();

		for ( SchemaTag schema : schemas )
		{
			dbSchemas.add( getDatabaseSchemaFor( schema ) );
		}

		return dbSchemas;
	}

	public DatabaseSchema getLatestSchema()
	{
		List localList = getSortedSchemas();
		if ( localList.size() == 0 )
			return null;
		return ( ( DatabaseSchema ) localList.get( localList.size() - 1 ) );
	}

	public DatabaseSchema getDatabaseSchemaFor( SchemaTag paramMetadataSchema )
	{
		Iterator localIterator = getDatabaseSchemas().iterator();
		while ( localIterator.hasNext() )
		{
			DatabaseSchema localDatabaseSchema = ( DatabaseSchema ) localIterator.next();
			if ( localDatabaseSchema.getSchema().equals( paramMetadataSchema ) )
				return localDatabaseSchema;
		}
		return null;
	}

	public List<DatabaseSchema> getLatestSchemas()
	{
		List<DatabaseSchema> schemas = getSortedSchemas();
		if ( schemas.size() == 0 )
			return null;
		Map<String, DatabaseSchema> latestSchemasMap = new HashMap<String, DatabaseSchema>();
		for ( DatabaseSchema schema : schemas )
		{
			SchemaTag metadataSchema = schema.getSchema();
			latestSchemasMap.put( metadataSchema.getProduct(), schema );
		}
		List<DatabaseSchema> latestSchemas = new ArrayList<DatabaseSchema>();
		for ( Map.Entry<String, DatabaseSchema> entry : latestSchemasMap.entrySet() )
		{
			latestSchemas.add( entry.getValue() );
		}
		return latestSchemas;
	}
}