package com.asjngroup.ncash.common.xml;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class XMLElementBinder
{
    public static XMLElement loadStructure( File file ) throws DocumentException
    {
        Document doc = XMLHelper.loadDocument( file );

        return new XMLElementBinder().bindStructures( doc );
    }

    public static XMLElement loadStructure( InputStream inputStream ) throws DocumentException
    {
        Document doc = XMLHelper.loadDocument( inputStream );

        return new XMLElementBinder().bindStructures( doc );
    }

    public static void saveStructure( File file, XMLElement XMLElement ) throws DocumentException, IOException
    {
        // create a document
        Document doc = DocumentFactory.getInstance().createDocument();

        // unbind the parse structure into the document
        new XMLElementBinder().unbindStructure( XMLElement, doc );

        // save the document to the file
        XMLHelper.saveDocument( file, doc );
    }

    public void unbindStructure( XMLElement XMLElement, Branch element )
    {
        // create a new element
        Element newElement = element.addElement( XMLElement.getName() );

        // set all the attributes
        for ( Map.Entry< String, String > entry : XMLElement.getProperties().entrySet() )
        {
            newElement.addAttribute( entry.getKey(), entry.getValue() );
        }

        // unbind all substructures
        for ( XMLElement subParseStructure : XMLElement.getSubStructures() )
        {
            unbindStructure( subParseStructure, newElement );
        }
    }

    public XMLElement bindStructures( Document doc )
    {
        return bindStructures( doc.getRootElement() );
    }

    public XMLElement bindStructures( Element rootElement )
    {
        XMLElement XMLElement = new XMLElement( rootElement.getName() );

        // add attributes as properties
        for ( Attribute attribute : (List< Attribute >)rootElement.attributes() )
        {
            XMLElement.addProperty( attribute.getName(), attribute.getValue() );
        }

        // iterate all sub elements and build record structures
        for ( Element element : (List< Element >)rootElement.elements() )
        {
            // build sub structures recursivley
            XMLElement subParseStructure = bindStructures( element );
            XMLElement.addSubStructure( subParseStructure );

        }

        return XMLElement;
    }
}
