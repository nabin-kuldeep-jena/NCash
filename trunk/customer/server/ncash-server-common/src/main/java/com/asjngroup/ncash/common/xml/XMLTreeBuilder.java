package com.asjngroup.ncash.common.xml;


import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.asjngroup.ncash.common.ObjectHelper;

public class XMLTreeBuilder
{
    private String packageName;
    private Document doc;

    private XMLTreeState state;

    private EntityResolver entityResolver = null;

    public XMLTreeBuilder( String packageName, Document doc )
    {
        this.packageName = packageName;
        this.doc = doc;
    }

    /*
     public void setAttributeResolver( String attributeName, AttributeResolver resolver )
     {

     }
     */
    public void setEntityResolver( EntityResolver entityResolver )
    {
        this.entityResolver = entityResolver;
    }

    public Object loadTree() throws XMLTreeException
    {
        Element rootElement = doc.getRootElement();

        state = new XMLTreeState();

        return buildElementSubTree( rootElement, 0 );
    }

    private Object buildElementSubTree( Element element, int level ) throws XMLTreeException
    {
        Class currentClass;
        try
        {
            currentClass = getClass( element.getName() );
        }
        catch ( ClassNotFoundException e )
        {
            // for the first level ignore the root element and return a list if the class is not found
            if ( level == 0 )
            {
                List list = new ArrayList();

                for ( Iterator< Element > it = element.elementIterator(); it.hasNext(); )
                {
                    Element currentSubElement = it.next();

                    Object currentObject = buildElementSubTree( currentSubElement, level + 1 );

                    list.add( currentObject );
                }

                return list;
            }
            else
            {
                throw new XMLTreeException( "Unable to load class %1 whilst building XML object tree", e, element.getName() );
            }
        }

        Object newObject;
        try
        {
            newObject = currentClass.newInstance();
        }
        catch ( InstantiationException e )
        {
            throw new XMLTreeException( "Unable to create instance of class %1 whilst building XML object tree", e, currentClass );
        }
        catch ( IllegalAccessException e )
        {
            throw new XMLTreeException( "Unable to create instance of class %1 whilst building XML object tree", e, currentClass );
        }

        // store the non-transient fields in a hash map
        Field[] fields = currentClass.getFields();
        Map< String, Field > fieldMap = new HashMap< String, Field >();

        for ( int i = 0; i < fields.length; i++ )
        {
            Field field = fields[ i ];

            // skip transient fields
            if ( Modifier.isTransient( field.getModifiers() ) )
                continue;

            fieldMap.put( field.getName(), field );
        }

        // loop over the XML attributes and load each one
        for ( Iterator it = element.attributeIterator(); it.hasNext(); )
        {
            Attribute currentAttribute = (Attribute)it.next();

            if ( fieldMap.containsKey( currentAttribute.getName() ) )
            {
                Field field = fieldMap.get( currentAttribute.getName() );

                Object newValue = XMLHelper.stringToType( field.getType(), currentAttribute.getValue() );

                try
                {
                    field.set( newObject, newValue );
                }
                catch ( IllegalAccessException e )
                {
                    throw new XMLTreeException( "Unable to set field value", e );
                }
            }
            else
            {
                //                throw new XMLTreeException( "Attribute %1 does not have matching field in object %2", currentAttribute.getName(), element.getName() );
            }
        }

        // add this element to the stack
        state.entityStack.add( newObject );

        // check for the magic "xmlTreeChildElements" field for storing all the elements
        if ( fieldMap.containsKey( "xmlTreeChildElements" ) )
        {
            Field field = fieldMap.get( "xmlTreeChildElements" );
            try
            {
                field.set( newObject, element.elements() );
            }
            catch ( IllegalAccessException e )
            {
                throw new XMLTreeException( "Unable to set xmlTreeChildElements.", e );
            }
        }

        // loop over sub elements and store them
        for ( Iterator iterator = element.elementIterator(); iterator.hasNext(); )
        {
            Element currentSubElement = (Element)iterator.next();

            if ( fieldMap.containsKey( currentSubElement.getName() ) )
            {
                Field field = fieldMap.get( currentSubElement.getName() );

                // if it's a list type then the sub element must just be a "wrapper" element with
                // no attributes
                if ( List.class.isAssignableFrom( field.getType() ) )
                {
                    List list;
                    try
                    {
                        list = (List)field.get( newObject );

                        if ( list == null )
                        {
                            list = new ArrayList();
                            field.set( newObject, list );
                        }
                    }
                    catch ( IllegalAccessException e )
                    {
                        throw new XMLTreeException( "Unable to set empty field list %1.", e, currentSubElement.getName() );
                    }

                    if ( currentSubElement.attributeCount() > 0 )
                    {
                        Object currentObject = buildElementSubTree( currentSubElement, level + 1 );

                        list.add( currentObject );
                    }
                    else
                    {
                        // iterate over all the elements WITHIN the current element and add them to the list
                        for ( Iterator it = currentSubElement.elementIterator(); it.hasNext(); )
                        {
                            Element currentSubSubElement = (Element)it.next();

                            Object currentObject = buildElementSubTree( currentSubSubElement, level + 1 );

                            list.add( currentObject );
                        }
                    }
                }
                else
                {
                    // not a list so recurse
                    Object currentObject = buildElementSubTree( currentSubElement, level + 1 );

                    try
                    {
                        field.set( newObject, currentObject );
                    }
                    catch ( IllegalAccessException e )
                    {
                        throw new XMLTreeException( "Unable to access field %1.", e, currentSubElement.getName() );
                    }
                }
            }
            else
            {
                //                throw new XMLTreeException( "Element %1 does not have matching field in object of type %2", currentSubElement.getName(), element.getName() );
            }
        }

        // remove it from the stack on return
        state.entityStack.remove( newObject );

        // call the entity resolver call back if it exists
        if ( entityResolver != null )
        {
            entityResolver.postUpdateEntityObject( state, newObject );
        }

        return newObject;
    }

    public void saveTree( Object baseTreeObject, Element currentElement ) throws XMLTreeException
    {
        try
        {
            Class clazz = baseTreeObject.getClass();

            Field[] fields = clazz.getDeclaredFields();

            Element element = currentElement.addElement( ObjectHelper.getClassOnlyName( clazz ) );

            for ( Field field : fields )
            {
                // skip transient fields
                if ( Modifier.isTransient( field.getModifiers() ) )
                    continue;

                if ( List.class.isAssignableFrom( field.getType() ) )
                {
                    Element listElement = element.addElement( field.getName() );

                    List list = (List)field.get( baseTreeObject );

                    if ( list != null )
                    {
                        for ( Object obj : list )
                        {
                            saveTree( obj, listElement );
                        }
                    }
                }
                else
                {
                    String newValue = XMLHelper.typeToString( field.get( baseTreeObject ) );
                    element.addAttribute( field.getName(), newValue );
                }
            }
        }
        catch ( IllegalAccessException e )
        {
            throw new XMLTreeException( e );
        }
    }

    private Class getClass( String className ) throws ClassNotFoundException
    {
        return Class.forName( packageName + "." + className );
    }

    public class XMLTreeState
    {
        public List< Object > entityStack = new ArrayList< Object >();
    }
}
