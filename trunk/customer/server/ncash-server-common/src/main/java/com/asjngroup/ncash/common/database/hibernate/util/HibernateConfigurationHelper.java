package com.asjngroup.ncash.common.database.hibernate.util;


import com.asjngroup.ncash.common.database.hibernate.exception.HibernateUtilException;
import com.asjngroup.ncash.common.util.ObjectHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.type.IntegerType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.Type;

import java.io.IOException;

public class HibernateConfigurationHelper
{
    public static PersistentClass getClassMapping( Configuration configuration, Class clazz )
    {
        Class toClazz = HibernateUtil.getMappedClassFromInterface( clazz );
        return configuration.getClassMapping( toClazz.getSimpleName() );
    }

    public static Map< Class, Class > buildImportMap( Configuration configuration ) throws HibernateUtilException
    {
        // build an import map for object creation
        Map< String, String > imports = configuration.getImports();
        Map< Class, Class > importMap = new HashMap< Class, Class >();

        for ( Map.Entry< String, String > entry : imports.entrySet() )
        {
            Class matchedInterface = null;
            Class toClazz;
            try
            {
                // value will be 'com.subex.spark.database.hibernate.mappings.reference.impl.ElementConnectionImpl'
                toClazz = Class.forName( entry.getValue() );
            }
            catch ( ClassNotFoundException e )
            {
                // skip any class not found in import map that doesn't refer to a class
                continue;
            }

            // get the class only name at the end ie 'ElementConnectionImpl'
            String classOnlyName = ObjectHelper.getClassOnlyName( toClazz );

            // strip the Impl off the end
            if ( classOnlyName.endsWith( "Impl" ) )
            {
                // now it will be 'ElementConnection'
                classOnlyName = classOnlyName.substring( 0, classOnlyName.length() - "Impl".length() );
            }

            Class[] interfaces = toClazz.getInterfaces();

            for ( Class intf : interfaces )
            {
                if ( classOnlyName.equals( ObjectHelper.getClassOnlyName( intf ) ) )
                {
                    matchedInterface = intf;
                }
            }

            if ( matchedInterface == null )
            {
                // ignore mapping for non-interface based class
                continue;
            }

            if ( !matchedInterface.isAssignableFrom( toClazz ) )
            {
                throw new HibernateUtilException( "Error creating custom import map. There is an import defined from class %1 to class %2 but the later cannot be assigned to the former", matchedInterface.getName(), toClazz.getName() );
            }

            // add to the map
            importMap.put( matchedInterface, toClazz );
        }

        return importMap;
    }

    public static Map< String, Class > buildClassNameInterfaceMap( Configuration configuration ) throws HibernateUtilException
    {
        Map< Class, Class > importMap = buildImportMap( configuration );

        return buildClassNameInterfaceMap( importMap );
    }

    public static Map< String, Class > buildClassNameInterfaceMap( Map< Class, Class > importMap ) throws HibernateUtilException
    {
        Map< String, Class > classNameInterfaceMap = new HashMap< String, Class >();
        Set< Class > importKeySet = importMap.keySet();

        for ( Class clazz : importKeySet )
        {
            classNameInterfaceMap.put( ObjectHelper.getClassOnlyName( clazz ), clazz );
        }

        return classNameInterfaceMap;
    }

    public static void addClassAndDependants( Configuration configuration, Class clazz ) throws MappingException
    {
        // map the class import
        Class toClazz;
        if ( clazz.isInterface() )
        {
            String toClazzName = clazz.getPackage().getName() + ".impl." + ObjectHelper.getClassOnlyName( clazz ) + "Impl";

            try
            {
                toClazz = Class.forName( toClazzName );
            }
            catch ( ClassNotFoundException e )
            {
                throw new MappingException( e );
            }
        }
        else
        {
            toClazz = clazz;
        }

        PersistentClass pClass = configuration.getClassMapping( toClazz.getSimpleName() );

        if ( pClass != null )
            return;

        // add the class
        String mappingsFilename = "/" + clazz.getName().replace( ".", "/" ) + ".hbm.xml";
        InputStream inputStream = clazz.getResourceAsStream( mappingsFilename );
        if ( inputStream == null )
        {
            throw new MappingException( "Could not find the xml mappings file " + mappingsFilename );
        }
        configuration.addInputStream( inputStream );

        pClass = configuration.getClassMapping( toClazz.getSimpleName() );

        // loop over all properties
        for ( Iterator iterator = pClass.getPropertyIterator(); iterator.hasNext(); )
        {
            Property property = (Property)iterator.next();

            Type type = property.getType();

            // for many to one types recursively add the class they point to
            if ( type instanceof ManyToOneType )
            {
                ManyToOneType manyToOneType = (ManyToOneType)type;

                addClassAndDependants( configuration, convertClassToInterface( manyToOneType.getReturnedClass() ) );
            }

            // migrate one to manys
            if ( type instanceof PersistentCollection )
            {
                Value value = property.getValue();
                if ( value instanceof Collection )
                {
                    Collection col = (Collection)value;

                    if ( col.getElement() instanceof OneToMany )
                    {
                        addClassAndDependants( configuration, convertClassToInterface( ( (OneToMany)col.getElement() ).getType().getClass() ) );
                    }
                }
            }
        }
    }

    private static Class convertClassToInterface( Class clazz )
    {
        if ( clazz.getInterfaces().length == 0 )
            return null;
        if ( clazz.getInterfaces().length == 1 )
            return clazz.getInterfaces()[ 0 ];

        for ( Class interfce : clazz.getInterfaces() )
        {
            if ( clazz.getName().startsWith( interfce.getName() ) )
                return interfce;
        }

        return null;
    }

    public static void addIdentifier( RootClass rootClass, String idPropertyName, String idColumnName ) throws MappingException
    {
        // the id property - default set to assigned Id, id
        Property idProperty = createProperty( rootClass, idPropertyName, idColumnName, IntegerType.INSTANCE, 0, true, true, true );
        rootClass.setIdentifierProperty( idProperty );
        SimpleValue idValue = ( (SimpleValue)idProperty.getValue() );
        idValue.setIdentifierGeneratorStrategy( "assigned" );

        // cribbed out of the Binder class
        Properties params = new Properties();

        params.setProperty( PersistentIdentifierGenerator.TABLE, idValue.getTable().getName() );
        params.setProperty( PersistentIdentifierGenerator.PK, ( (Column)idValue.getColumnIterator().next() ).getName() );

        idValue.setIdentifierGeneratorProperties( params );

        idValue.getTable().setIdentifierValue( idValue );

        idValue.setNullValue( "null" );

        rootClass.setIdentifier( idValue );

        rootClass.createPrimaryKey();
    }

    // persistance class helpers
    public static RootClass createRootClass( Mappings mappings, String name, String table, boolean lazy ) throws MappingException
    {
        // new class
        RootClass pClass = new RootClass();

        // set the class
        Class clazz = null;
        try
        {
            clazz = Class.forName( name );
        }
        catch ( ClassNotFoundException e )
        {
            throw new MappingException( e );
        }

        pClass.setClassName( clazz.getSimpleName() );

        // do the laziness
        if ( lazy )
        {
            pClass.setProxyInterfaceName( clazz.getSimpleName() );
        }

        // set the discriminator to default ( itself )
        pClass.setDiscriminatorValue( pClass.getClassName() );

        // set the dynamics to false
        pClass.setDynamicInsert( false );
        pClass.setDynamicUpdate( false );

        // This needs to be added to the Mappings overall object

        // this uses the internal API, may break in a later version of hibernate!!
        pClass.setOptimisticLockMode( OptimisticLockStyle.VERSION.getOldCode() );

        // set the table
        Table tableObj = mappings.addTable( null, null, table,null, false);
        pClass.setTable( tableObj );

        // some options
        pClass.setMutable( true );
        pClass.setExplicitPolymorphism( false );

        // version??

        return pClass;
    }

    // property helpers
    public static void addProperty( PersistentClass pClass, String propertyName, String columnName, Type type ) throws MappingException
    {
        addProperty( pClass, propertyName, columnName, type, 0, true );
    }

    public static void addProperty( PersistentClass pClass, String propertyName, String columnName, Type type, int length ) throws MappingException
    {
        addProperty( pClass, propertyName, columnName, type, length, true );
    }

    public static void addProperty( PersistentClass pClass, String propertyName, String columnName, Type type, int length, boolean nullable ) throws MappingException
    {
        addProperty( pClass, propertyName, columnName, type, length, nullable, true, true );
    }

    public static void addProperty( PersistentClass pClass, String propertyName, String columnName, Type type, int length, boolean nullable, boolean insert, boolean update ) throws MappingException
    {
        Property property = createProperty( pClass, propertyName, columnName, type, length, nullable, insert, update );
        pClass.addProperty( property );
    }

    public static Property createProperty( PersistentClass pClass, String propertyName, String columnName, Type type, int length, boolean nullable, boolean insert, boolean update ) throws MappingException
    {
        // create the column
        Column column = new Column();
        //column.setType( type.get );
        column.setTypeIndex( 0 );
        column.setNullable( nullable );
        column.setUnique( false );
        column.setSqlType( null );
        column.setName( columnName );
        column.setLength( length );

        Table table = pClass.getTable();

        // create value
        SimpleValue value = new SimpleValue(null, table );
        value.setTypeName( type.getName() );

        // add column to table and value
        table.addColumn( column );
        value.addColumn( column );

        // create the property
        Property property = new Property();
        property.setValue( value );
        property.setName( propertyName );
        property.setInsertable( insert );
        property.setUpdateable( update );
        property.setCascade( "none" );
        property.setMetaAttributes( new HashMap() );

        return property;
    }

    public static void mergePropertyToClass( PersistentClass pClass, Property property ) throws MappingException
    {
        addProperty( pClass, property.getName(), ( (Column)property.getColumnIterator().next() ).getName(), property.getType(), ( (Column)property.getColumnIterator().next() ).getLength(), property.isOptional(), property.isInsertable(),
                property.isUpdateable() );
    }

    public static Map< Class, Map< String, Class > > buildRelationshipMappings( Configuration configuration ) throws MappingException
    {
        Map< Class, Map< String, Class > > relationshipMappings = new HashMap< Class, Map< String, Class > >();

        for ( Iterator< PersistentClass > it = configuration.getClassMappings(); it.hasNext(); )
        {
            PersistentClass persistentClass = it.next();

            java.util.Map< String, Class > columnsToClass = new HashMap< String, Class >();
            java.util.Map< String, String > columnsToProperties = new HashMap< String, String >();

            // create the relationship map and add it to the global map
            java.util.Map< String, Class > classRelationships = new HashMap< String, Class >();
            relationshipMappings.put( persistentClass.getMappedClass(), classRelationships );

            // search all the properties for many to one types, and store the columns they refer to
            for ( Iterator< Property > it2 = persistentClass.getPropertyIterator(); it2.hasNext(); )
            {
                Property property = it2.next();

                if ( property.getType() instanceof ManyToOneType )
                {
                    if ( property.getColumnSpan() != 1 )
                    {
                        // silently ignore?
                        continue;
                    }

                    columnsToClass.put( ( (Column)property.getColumnIterator().next() ).getName(), property.getType().getReturnedClass() );
                }
                else if ( property.getType() instanceof PrimitiveType )
                {
                    if ( property.getColumnSpan() != 1 )
                    {
                        // silently ignore?
                        continue;
                    }

                    columnsToProperties.put( ( (Column)property.getColumnIterator().next() ).getName(), property.getName() );
                }
            }

            // loop over all the columns->class and find the matching property from the column->property
            for ( java.util.Map.Entry< String, Class > entry : columnsToClass.entrySet() )
            {
                String propertyName = columnsToProperties.get( entry.getKey() );

                if ( propertyName == null )
                {
                    throw new MappingException( "Found a many-to-one without a matching property. Column = " + entry.getKey() );
                }

                classRelationships.put( propertyName, entry.getValue() );
            }
        }

        return relationshipMappings;
    }

    public static List< String > propertiesToColumns( Configuration configuration, Class clazz, List< String > propertyNames ) throws HibernateUtilException
    {
        // get's all columns for property names for the class
        PersistentClass persistentClass = configuration.getClassMapping( clazz.getSimpleName() );

        List< String > columns = new ArrayList< String >();

        for ( String propertyName : propertyNames )
        {
            Property property = null;
            try
            {
                property = persistentClass.getProperty( propertyName );
            }
            catch ( MappingException e )
            {
                if ( property == null )
                {
                    property = persistentClass.getIdentifierProperty();

                    if ( !property.getName().equals( propertyName ) )
                    {
                        throw new HibernateUtilException( "Could not find column for property '%1' in class '%2'", property, clazz.getName() );
                    }
                }
            }

            for ( Iterator it = property.getColumnIterator(); it.hasNext(); )
            {
                Column column = (Column)it.next();

                columns.add( column.getName() );
            }
        }

        return columns;
    }
}