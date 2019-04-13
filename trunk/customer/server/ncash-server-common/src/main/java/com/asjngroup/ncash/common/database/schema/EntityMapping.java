package com.asjngroup.ncash.common.database.schema;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.asjngroup.ncash.common.util.StringHelper;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 10:13:22
 */
public class EntityMapping implements Cloneable
{
    public String EntityName;
    public String DatabaseName = "";
    public String TableName = "";
    public String ServerPackageName = "";
    public String ClientAssemblyName = "";
    public String ClientNamespace = "";

    public List< FieldMapping > FieldMappings = new ArrayList< FieldMapping >();
    public List< NestedObject > NestedObjects = new ArrayList< NestedObject >();
    public List< NestedCollection > NestedCollections = new ArrayList< NestedCollection >();

    public String PrimaryKey;
    public String VersionKey;
    public String BusinessConstraints;

    public transient Schema schema;


    public Object clone() throws CloneNotSupportedException
    {
    	EntityMapping entityMapping = new EntityMapping();

        entityMapping.EntityName = EntityName;
        entityMapping.DatabaseName = DatabaseName;
        entityMapping.TableName = TableName;

        entityMapping.PrimaryKey = PrimaryKey;
        entityMapping.VersionKey = VersionKey;

        for ( FieldMapping fieldMapping : FieldMappings )
        {
            entityMapping.FieldMappings.add( (FieldMapping)fieldMapping.clone() );
        }

        for ( NestedObject nestedObject : NestedObjects )
        {
            entityMapping.NestedObjects.add( (NestedObject)nestedObject.clone() );
        }

        for ( NestedCollection nestedCollection : NestedCollections )
        {
            entityMapping.NestedCollections.add( (NestedCollection)nestedCollection.clone() );
        }

        return entityMapping;
    }

    public String[] getBusinessConstraints()
    {
        if ( BusinessConstraints.trim().length() == 0 )
            return new String[0];

        return StringHelper.split( BusinessConstraints, ',' );
    }

    public FieldMapping findFieldMappingByColumn( String columnName )
    {
        for ( FieldMapping fieldMapping : FieldMappings )
        {
            if ( fieldMapping.ColumnName.equals( columnName ) )
            {
                return fieldMapping;
            }
        }

        return null;
    }

    public FieldMapping findPropertyMapping( String propertyName )
    {
        for ( FieldMapping propertyMapping : FieldMappings )
        {
            if ( propertyMapping.FieldName.equals( propertyName ) )
            {
                return propertyMapping;
            }
        }

        return null;
    }

    public NestedObject findNestedObject( String propertyName )
    {
        for ( NestedObject nestedObject : NestedObjects )
        {
            if ( nestedObject.PropertyName.equals( propertyName ) )
            {
                return nestedObject;
            }
        }

        return null;
    }

    public NestedCollection findNestedCollection( String propertyName )
    {
        for ( NestedCollection nestedCollection : NestedCollections )
        {
            if ( nestedCollection.PropertyName.equals( propertyName ) )
            {
                return nestedCollection;
            }
        }

        return null;
    }

    public Table getTable()
    {
        return schema.Databases.get( 0 ).findTable( TableName );
    }
}
