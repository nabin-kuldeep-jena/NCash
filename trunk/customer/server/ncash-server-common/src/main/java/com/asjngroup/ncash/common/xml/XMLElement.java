package com.asjngroup.ncash.common.xml;

import java.util.Map;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.IOException;

public class XMLElement
{
    private String name;
    private Map< String, String > properties = new LinkedHashMap< String, String >();
    private List< XMLElement > subRecordStructures = new ArrayList< XMLElement >();

    public XMLElement()
    {
    }

    public XMLElement( String name )
    {
        this.name = name;
    }

    public boolean hasProperty( String property )
    {
        // see if the property exists
        if ( !properties.containsKey( property ) )
            return false;

        // get the property and check it is not empty
        return getProperty( property ).length() > 0;
    }

    public String getProperty( String property )
    {
        return getProperty( property, false );
    }

    public String getProperty( String property, boolean required )
    {
        if ( required && !hasProperty( property ) )
        {
            throw new NCashRuntimeException( "The property '%1' is required but was not specified", property );
        }

        return properties.get( property );
    }

    public Integer getIntegerProperty( String property )
    {
        return getIntegerProperty( property, false );
    }

    public Integer getIntegerProperty( String property, boolean required )
    {
        String str = getProperty( property, required );
        if ( str == null || str.length() == 0 )
            return null;

        return Integer.parseInt( str );
    }

    public boolean getBooleanProperty( String property, boolean defaultValue )
    {
        return getBooleanProperty( property, false, defaultValue );
    }

    public boolean getBooleanProperty( String property, boolean required, boolean defaultValue )
    {
        String str = getProperty( property, required );
        if ( str == null || str.length() == 0 )
            return defaultValue;

        return str.equalsIgnoreCase( "true" );
    }

    public boolean hasSubStructure( String structureName )
    {
        for ( XMLElement XMLElement : subRecordStructures )
        {
            if ( XMLElement.getName().equals( structureName ) )
            {
                return true;
            }
        }

        return false;
    }

    public XMLElement getSubStructure( String structureName )
    {
        return getSubStructure( structureName, false );
    }

    public XMLElement getSubStructure( String structureName, boolean required )
    {
        for ( XMLElement XMLElement : subRecordStructures )
        {
            if ( XMLElement.getName().equals( structureName ) )
            {
                return XMLElement;
            }
        }

        if ( required )
            throw new NCashRuntimeException( "Could not find required substructure '%1'", structureName );

        return null;
    }

    public List< XMLElement > getSubStructures( String structureName )
    {
        List< XMLElement > parseStructures = new ArrayList< XMLElement >();

        for ( XMLElement XMLElement : subRecordStructures )
        {
            if ( XMLElement.getName().equals( structureName ) )
            {
                parseStructures.add( XMLElement );
            }
        }

        return parseStructures;
    }

    public void addSubStructure( XMLElement XMLElement )
    {
        subRecordStructures.add( XMLElement );
    }

    public XMLElement createSubStructure( String name )
    {
        XMLElement XMLElement = new XMLElement( name );

        addSubStructure( XMLElement );

        return XMLElement;
    }

    public void addProperty( String name, String value )
    {
        properties.put( name, value );
    }

    public List< XMLElement > getSubStructures()
    {
        return subRecordStructures;
    }

    public String getName()
    {
        return name;
    }


    public Map< String, String > getProperties()
    {
        return properties;
    }

    public XMLElement clone()
    {
        XMLElement copy = new XMLElement( name );

        for ( Map.Entry< String, String > entry : properties.entrySet() )
        {
            copy.addProperty( entry.getKey(), entry.getValue() );
        }

        for ( XMLElement subStructure : subRecordStructures )
        {
            copy.addSubStructure( subStructure.clone() );
        }

        return copy;
    }
}
