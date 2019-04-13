package com.asjngroup.ncash.common.database.schema;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.asjngroup.ncash.common.ObjectHelper;
import com.asjngroup.ncash.common.xml.EntityResolver;
import com.asjngroup.ncash.common.xml.XMLHelper;
import com.asjngroup.ncash.common.xml.XMLTreeBuilder;
import com.asjngroup.ncash.common.xml.XMLTreeException;

/**
 * User: nabin.jena
 * Date: 09-Apr-2017
 * Time: 19:33:21
 */
public class Schema implements Cloneable
{
    public String SchemaName;
    public int MajorVersion;
    public int MinorVersion;
    public int RevisionVersion;
    public int ServicePack;
    public boolean IsExtension;
    public String Namespace;
    public String ServerPackageName;
    public String ClientAssemblyName;
    public String ClientNamespace;
    public String InstanceName;

    public List< Database > Databases = new ArrayList< Database >();
    public List< Relationship > Relationships = new ArrayList< Relationship >();
    public List< EntityMapping > EntityMappings = new ArrayList< EntityMapping >();

    public Object clone() throws CloneNotSupportedException
    {
        Schema schema = new Schema();

        schema.SchemaName = SchemaName;
        schema.MajorVersion = MajorVersion;
        schema.MinorVersion = MinorVersion;
        schema.RevisionVersion = RevisionVersion;
        schema.ServicePack = ServicePack;
        schema.IsExtension = IsExtension;
        schema.InstanceName = InstanceName;

        for ( Database database : Databases )
        {
            schema.Databases.add( (Database)database.clone() );
        }

        for ( Relationship relationship : Relationships )
        {
            schema.Relationships.add( (Relationship)relationship.clone() );
        }

        for ( EntityMapping objectMapping : EntityMappings )
        {
            schema.EntityMappings.add( (EntityMapping)objectMapping.clone() );
        }

        return schema;
    }

    public Table findTable( String databasename, String tablename )
    {
        Database database = findDatabase( databasename );

        if ( database == null )
            return null;

        return database.findTable( tablename );
    }

    public Database findDatabase( String databaseName )
    {
        for ( Database database : Databases )
        {
            if ( database.DatabaseName.equals( databaseName ) )
            {
                return database;
            }
        }

        return null;
    }

    public EntityMapping findEntityMapping( String entityName )
    {
        for ( EntityMapping entityMapping : EntityMappings )
        {
            if ( entityMapping.EntityName.equals( entityName ) )
            {
                return entityMapping;
            }
        }

        return null;
    }

    public EntityMapping findEntityMappingWithPrimaryKey( String primaryKey )
    {
        for ( EntityMapping entityMapping : EntityMappings )
        {
            if ( primaryKey.endsWith( entityMapping.PrimaryKey ) )
            {
                return entityMapping;
            }
        }

        return null;
    }

    public Table findTableFromEntityName( String database, String objectName )
    {
    	EntityMapping enetityMapping = findEntityMapping( objectName );

        if ( enetityMapping == null )
            return null;

        return findTable( database, enetityMapping.TableName );
    }

    public EntityMapping findEntityMappingFromTableName( String database, String tableName )
    {
        for ( EntityMapping entityMapping : EntityMappings )
        {
            if ( entityMapping.TableName.equals( tableName ) && entityMapping.DatabaseName.equals( database ) )
            {
                return entityMapping;
            }
        }

        return null;
    }

    public EntityMapping findEntityMappingFromClass( Class clazz )
    {
        return findEntityMapping( ObjectHelper.getClassOnlyName( clazz ) );
    }

    public static Schema findSchema( List< Schema > schemas, String objectName )
    {
        // attempt to find the schema this nested object's object belongs to
        for ( Schema schema : schemas )
        {
            if ( schema.findEntityMapping( objectName ) != null )
                return schema;
        }

        // not found
        return null;
    }

    public void mergeSchema( Schema mergeSchema )
    {
        for ( Database mergeDatabase : mergeSchema.Databases )
        {
            Database localDatabase = findDatabase( mergeDatabase.DatabaseName );

            // no local database found so add the entire database in
            if ( localDatabase == null )
            {
                Databases.add( mergeDatabase );
                continue;
            }

            // compare tables
            for ( Table mergeTable : mergeDatabase.Tables )
            {
                Table table = localDatabase.findTable( mergeTable.TableName );

                // no local table found so add the merge one
                if ( table == null )
                {
                    localDatabase.Tables.add( mergeTable );
                    mergeTable.database = localDatabase;
                    continue;
                }

                // compares indicis
                for ( Index mergeIndex : mergeTable.Indexes )
                {
                    Index index = table.findIndex( mergeIndex.IndexName );

                    // add the missing index
                    if ( index == null )
                    {
                        table.Indexes.add( mergeIndex );
                        mergeIndex.table = table;
                        continue;
                    }
                }
            }
        }

        for ( EntityMapping mergeEntityMapping : mergeSchema.EntityMappings )
        {
        	EntityMapping objectMapping = findEntityMapping( mergeEntityMapping.EntityName );

            if ( objectMapping == null )
            {
            	EntityMappings.add( mergeEntityMapping );
            }
        }
    }

    public void changeDatabaseName( String databaseName )
    {
        if ( Databases.size() > 1 )
        {
            throw new UnsupportedOperationException( "Cannot change database name on multiple database schema" );
        }

        // change the db names in the schema and object mappings
        Databases.get( 0 ).DatabaseName = databaseName;
        for ( EntityMapping entityMapping : EntityMappings )
        {
            entityMapping.DatabaseName = databaseName;
        }
    }

    public static Schema loadSchema( String filename ) throws SchemaParseException
    {
        try
        {
            Document document = XMLHelper.loadDocument( new File( filename ) );

            return loadSchema( document );
        }
        catch ( DocumentException e )
        {
            throw new SchemaParseException( e );
        }
    }

    public static Schema loadSchema( InputStream stream ) throws SchemaParseException
    {
        try
        {
            Document document = XMLHelper.loadDocument( stream );

            return loadSchema( document );
        }
        catch ( DocumentException e )
        {
            throw new SchemaParseException( e );
        }
    }

    public static Schema loadSchema( Document document ) throws SchemaParseException
    {
        try
        {
            XMLTreeBuilder tree = new XMLTreeBuilder( "com.asjngroup.ncash.common.database.schema", document );
            tree.setEntityResolver( new Schema.SchemaEntityResolver() );
            Schema schema = (Schema)tree.loadTree();

            return schema;
        }
        catch ( XMLTreeException e )
        {
            throw new SchemaParseException( e );
        }
    }

    static class SchemaEntityResolver implements EntityResolver
    {
        public void postUpdateEntityObject( XMLTreeBuilder.XMLTreeState stack, Object entityObject ) throws XMLTreeException
        {
            if ( entityObject instanceof Table )
            {
                Table table = (Table)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof Database )
                    {
                        table.database = (Database)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found a table %1 that is not within a database", table.TableName );
            }

            
            if ( entityObject instanceof Column )
            {
                Column column = (Column)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof Table )
                    {
                        column.table = (Table)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found a column %1 that is not within a table", column.ColumnName );
            }

            if ( entityObject instanceof Index )
            {
                Index index = (Index)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof Table )
                    {
                        index.table = (Table)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found an index %1 that is not within a table", index.IndexName );
            }

            if ( entityObject instanceof EntityMapping )
            {
            	EntityMapping entityMapping = (EntityMapping)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof Schema )
                    {
                        entityMapping.schema = (Schema)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found an object mapping %1 that is not within a schema", entityMapping.EntityName );
            }

            if ( entityObject instanceof FieldMapping )
            {
                FieldMapping fieldMapping = (FieldMapping)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof EntityMapping )
                    {
                    	fieldMapping.entityMapping = (EntityMapping)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found a property mapping %1 that is not within an object mapping", fieldMapping.FieldName );
            }

            if ( entityObject instanceof NestedObject )
            {
                NestedObject nestedObject = (NestedObject)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof EntityMapping )
                    {
                        nestedObject.entityMapping = (EntityMapping)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found a nested object %1 that is not within an object mapping", nestedObject.PropertyName );
            }

            if ( entityObject instanceof NestedCollection )
            {
                NestedCollection nestedCollection = (NestedCollection)entityObject;

                for ( Object obj : stack.entityStack )
                {
                    if ( obj instanceof EntityMapping )
                    {
                        nestedCollection.entityMapping = (EntityMapping)obj;
                        return;
                    }
                }

                throw new XMLTreeException( "Found a nested collection %1 that is not within an object mapping", nestedCollection.PropertyName );
            }
        }
    }
}
