package com.asjngroup.ncash.common.database.schema;


import java.io.IOException;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 11:18:22
 */
public class FieldMapping implements Cloneable
{
    public String FieldName;
    public String ColumnName;
    public String NestedObjectName;

    public transient EntityMapping entityMapping;

    public Object clone() throws CloneNotSupportedException
    {
    	FieldMapping propertyMapping = new FieldMapping();

        propertyMapping.FieldName = FieldName;
        propertyMapping.ColumnName = ColumnName;
        propertyMapping.NestedObjectName = NestedObjectName;

        return propertyMapping;
    }

    public Column getColumn()
    {
        return entityMapping.schema.Databases.get( 0 ).findTable( entityMapping.TableName ).findColumn( ColumnName );
    }

    public boolean isNestedObjectProperty()
    {
        for ( NestedObject nestedObject : entityMapping.NestedObjects )
        {
            if ( nestedObject.PropertyName.equals( FieldName ) )
            {
                return true;
            }
        }

        return false;
    }
}
