package com.asjngroup.ncash.common.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.asjngroup.ncash.common.io.util.FileHelper;
import com.asjngroup.ncash.common.util.collection.CollectionHelper;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;


public class XMLHelper
{
    private static final DateTimeFormatter defaultDateTimeFormatter = DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss" );

    public static SAXReader createSAXReader( EntityResolver entityResolver )
    {
        SAXReader reader = new SAXReader();
        reader.setMergeAdjacentText( true );
        if ( entityResolver != null )
        {
            reader.setEntityResolver( entityResolver );
        }
        return reader;
    }

    public static Document loadDocument( InputStream stream, EntityResolver entityResolver ) throws DocumentException
    {
        return createSAXReader( entityResolver ).read( stream );
    }

    public static Document loadDocument( InputStream stream ) throws DocumentException
    {
        return createSAXReader( null ).read( stream );
    }

    public static Document loadDocument( Reader reader, EntityResolver entityResolver ) throws DocumentException
    {
        return createSAXReader( entityResolver ).read( reader );
    }

    public static Document loadDocument( Reader reader ) throws DocumentException
    {
        return createSAXReader( null ).read( reader );
    }

    public static Document loadDocument( File file, EntityResolver entityResolver ) throws DocumentException
    {
        return createSAXReader( entityResolver ).read( file );
    }

    public static Document loadDocument( File file ) throws DocumentException
    {
        return createSAXReader( null ).read( file );
    }

    public static void saveDocument( String filename, Document doc ) throws IOException
    {
        OutputFormat outputFormat = new OutputFormat( "    ", true );
        outputFormat.setOmitEncoding( true );
        
        Charset c = FileHelper.unicodeCharset;
        OutputStream out = new FileOutputStream(filename);
        OutputStreamWriter writer;
        
        if (FileHelper.isUnicode || FileHelper.isUnicodeFileEncoding)
        	writer = new OutputStreamWriter(out, c);
        else
        	writer = new OutputStreamWriter(out);
        
        XMLWriter xmlWriter = new XMLWriter( writer, outputFormat );
        xmlWriter.write( doc );
        xmlWriter.flush();
        xmlWriter.close();
        writer.close();
    }

    public static void saveDocument( File file, Document doc ) throws IOException
    {
        OutputFormat outputFormat = new OutputFormat( "    ", true );
        outputFormat.setOmitEncoding( true );
        
        Charset c = FileHelper.unicodeCharset;
        OutputStream out = new FileOutputStream(file);
        OutputStreamWriter writer;
        
        if (FileHelper.isUnicode || FileHelper.isUnicodeFileEncoding)
        	writer = new OutputStreamWriter(out, c);
        else
        	writer = new OutputStreamWriter(out);
        
        XMLWriter xmlWriter = new XMLWriter( writer, outputFormat );
        xmlWriter.write( doc );
        xmlWriter.flush();
        xmlWriter.close();
        writer.close();
    }

    public static String saveDocument( Document doc ) throws IOException
    {
        StringWriter writer = new StringWriter();
        OutputFormat outputFormat = new OutputFormat( "    ", true );
        //        outputFormat.setSuppressDeclaration( true );
        outputFormat.setOmitEncoding( true );
        XMLWriter xmlWriter = new XMLWriter( writer, outputFormat );
        xmlWriter.write( doc );
        xmlWriter.flush();
        xmlWriter.close();
        writer.close();
        return writer.toString();
    }

    public static org.w3c.dom.Document dom4jDocumentToW3CDocument( Document document, EntityResolver entityResolver ) throws DocumentException
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactoryImpl.newInstance();
        DocumentBuilder builder = null;
        try
        {
            builder = builderFactory.newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new DocumentException( e );
        }

        StringWriter writer = new StringWriter();
        OutputFormat outputFormat = new OutputFormat( "    ", true );
        //        outputFormat.setSuppressDeclaration( true );
        outputFormat.setOmitEncoding( true );
        XMLWriter xmlWriter = new XMLWriter( writer, outputFormat );

        try
        {
            xmlWriter.write( document );
            xmlWriter.flush();
            xmlWriter.close();
        }
        catch ( IOException e )
        {
            throw new DocumentException( e );
        }

        try
        {
            builder.setEntityResolver( entityResolver );
            org.w3c.dom.Document newDoc = builder.parse( new ByteArrayInputStream( writer.toString().getBytes() ) );
            return newDoc;
        }
        catch ( SAXException e )
        {
            throw new DocumentException( e );
        }
        catch ( IOException e )
        {
            throw new DocumentException( e );
        }
    }

    public static String getValue( Element element, String attribute )
    {
        // Return the attribute value
        Attribute attr = element.attribute( attribute );
        return ( attr == null ? null : attr.getValue() );
    }

    public static String getValue( Element element, String attribute, String defaultValue )
    {
        // Return the attribute value
        String value = getValue( element, attribute );
        return ( value == null ? defaultValue : value );
    }

    public static Element findElement( Element element, String name )
    {
        // recursively search for the first element under 'element' of the specified name
        if ( element.getName().equals( name ) )
            return element;

        if ( element.element( name ) != null )
            return element.element( name );

        for ( Iterator it = element.elementIterator(); it.hasNext(); )
        {
            Element childElement = (Element)it.next();

            Element result = findElement( childElement, name );

            if ( result != null )
                return result;
        }
        return null;
    }

    public static Element findElementByAttributes( List< Element > elements, String key, String value )
    {
        Map< String, String > attributeValues = new HashMap< String, String >();
        attributeValues.put( key, value );

        // overloaded method
        return findElementByAttributes( elements, attributeValues );
    }

    public static Element findElementByAttributes( List< Element > elements, Map< String, String > attributeValues )
    {
        // iterate each element under the specified one
        for ( Element childElement : elements )
        {
            boolean matched = true;

            // iterate the search attributes for each element
            for ( Map.Entry< String, String > entry : attributeValues.entrySet() )
            {
                Attribute attribute = childElement.attribute( entry.getKey() );

                // no attribute of this name, so no match
                if ( attribute == null )
                {
                    matched = false;
                    break;
                }

                // attribute value does not match
                if ( !attribute.getValue().equals( entry.getValue() ) )
                {
                    matched = false;
                    break;
                }
            }

            if ( matched )
                return childElement;
        }

        return null;
    }

    public static Element findElementByAttributes( Element element, String key, String value )
    {
        Map< String, String > attributeValues = new HashMap< String, String >();
        attributeValues.put( key, value );

        // overloaded method
        return findElementByAttributes( element, attributeValues );
    }

    public static Element findElementByAttributes( Element element, Map< String, String > attributeValues )
    {
        return findElementByAttributes( element.elements(), attributeValues );
    }

    public static Object stringToType( Class toClass, String str )
    {
        if ( toClass.equals( Integer.class ) || toClass.equals( Integer.TYPE ) )
        {
            if ( str.length() == 0 )
                return null;
            return Integer.parseInt( str );
        }

        if ( toClass.equals( Boolean.class ) || toClass.equals( Boolean.TYPE ) )
        {
            if ( str.equals( "Y" ) )
                return true;
            if ( str.equals( "N" ) )
                return false;
            if ( str.equals( "1" ) )
                return true;
            if ( str.equals( "0" ) )
                return false;
            if ( str.equals( "true" ) )
                return true;
            if ( str.equals( "false" ) )
                return false;

            throw new IllegalArgumentException( "Unable to parse value to boolean: " + str );
        }

        if ( toClass.equals( String.class ) )
        {
            return str;
        }

        if ( toClass.equals( DateTime.class ) )
        {
            if ( str.length() == 0 )
                return null;
            return defaultDateTimeFormatter.parseDateTime( str );
        }

        if ( toClass.equals( BigDecimal.class ) )
        {
            return new BigDecimal( str );
        }

        if ( toClass.equals( Long.class ) || toClass.equals( Long.TYPE ) )
        {
            if ( str.length() == 0 )
                return null;
            return Long.parseLong( str );
        }

        if ( toClass.equals( Short.class ) || toClass.equals( Short.TYPE ) )
        {
            if ( str.length() == 0 )
                return null;
            return Short.parseShort( str );
        }

        if ( toClass.equals( Byte.class ) || toClass.equals( Byte.TYPE ) )
        {
            if ( str.length() == 0 )
                return null;
            return Byte.parseByte( str );
        }

        if ( toClass.isEnum() )
        {
            Object[] enums = toClass.getEnumConstants();

            for ( Object enumObj : enums )
            {
                if ( enumObj.toString().equals( str ) )
                {
                    return Enum.valueOf( toClass, str );
                }
            }

            throw new IllegalArgumentException( "Unable to parse value " + str + " to enum " + toClass.getName() );
        }

        throw new IllegalArgumentException( "Unable to parse value. Unknown receiving type: " + toClass.getName() );
    }

    public static String typeToString( Object obj )
    {
        if ( obj instanceof Boolean )
        {
            boolean bool = ( (Boolean)obj ).booleanValue();

            if ( bool )
                return "Y";
            else
                return "N";
        }
        else if ( obj instanceof DateTime )
        {
            return defaultDateTimeFormatter.print( (DateTime)obj );
        }

        if ( obj == null )
            return "";

        return obj.toString();
    }

    // scans an element tree and returns all distinct element names
    public static List< String > getAllSubElementNames( Element element )
    {
        List< String > elementNames = new ArrayList< String >();

        for ( Iterator< Element > it = element.elementIterator(); it.hasNext(); )
        {
            Element subElement = it.next();

            // add the element name to the list if it doesn't already contain it
            if ( !elementNames.contains( subElement.getName() ) )
            {
                elementNames.add( subElement.getName() );
            }

            // recurse down
            List< String > subElementNames = getAllSubElementNames( subElement );

            // merge the lists ignoring duplicates
            elementNames = CollectionHelper.unionList( elementNames, subElementNames );
        }

        return elementNames;
    }
}
