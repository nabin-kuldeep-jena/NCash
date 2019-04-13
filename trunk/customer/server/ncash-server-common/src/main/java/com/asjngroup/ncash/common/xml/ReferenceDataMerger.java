package com.asjngroup.ncash.common.xml;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Attribute;

import java.util.*;
import java.io.InputStream;

public class ReferenceDataMerger
{
    private Map< String, List< String > > searchKeyMap;
    private Document mainDoc;
    private List< Document > mergeDocuments = new ArrayList< Document >();

    public void initialise( Map< String, List< String > > searchKeyMap, List< String > filenames ) throws XMLException
    {
        this.searchKeyMap = searchKeyMap;

        if ( filenames.size() == 0 )
        {
            throw new XMLException( "No source files specified" );
        }

        // load the first document
        String mainDocumentFilename = filenames.get( 0 );

        // load the first document
        try
        {
            InputStream documentStream = getClass().getResourceAsStream( mainDocumentFilename );
            if ( documentStream == null )
            {
                throw new XMLException( "Could not find xml document %1", mainDocumentFilename );
            }
            mainDoc = XMLHelper.loadDocument( documentStream );
        }
        catch ( DocumentException e )
        {
            throw new XMLException( "Error loading main XML document %1", e, mainDocumentFilename );
        }

        for ( int i = 1; i < filenames.size(); i++ )
        {
            // load the merge document
            String mergeDocumentFilename = filenames.get( i );

            // merge document on top
            Document mergeDoc;
            try
            {
                InputStream documentStream = getClass().getResourceAsStream( mergeDocumentFilename );
                if ( documentStream == null )
                {
                    throw new XMLException( "Could not find xml document %1", mergeDocumentFilename );
                }
                mergeDoc = XMLHelper.loadDocument( documentStream );
            }
            catch ( DocumentException e )
            {
                throw new XMLException( "Error loading merge XML document %1", e, mergeDocumentFilename );
            }

            if ( !mergeDoc.getRootElement().getName().equals( mainDoc.getRootElement().getName() ) )
            {
                throw new XMLException( "Merge document %1 has different root element to main document %2", mergeDocumentFilename, mainDocumentFilename );
            }

            mergeDocuments.add( mergeDoc );
        }
    }

    public Document getMainDoc()
    {
        return mainDoc;
    }

    public List< Document > getMergeDocuments()
    {
        return mergeDocuments;
    }

    public void mergeDocuments() throws XMLException
    {
        // for all merge documents and all "root" elements merge in.
        for ( Document mergeDoc : mergeDocuments )
        {
            // root element is just an identifying tag which must match on all documents
            if ( !mainDoc.getRootElement().getName().equals( mergeDoc.getRootElement().getName() ) )
            {
                throw new XMLException( "Merge document root element '%1' does not match main document root element '%2'", mergeDoc.getRootElement().getName(), mainDoc.getRootElement().getName() );
            }

            // do xml merging from the first level
            doMergeLevel( mainDoc.getRootElement(), mergeDoc.getRootElement(), 0 );
        }
    }

    private void doMergeLevel( Element mainElement, Element mergeElement, int level ) throws XMLException
    {
        for ( Iterator it = mergeElement.elementIterator(); it.hasNext(); )
        {
            Element currentMergeElement = (Element)it.next();

            // if the element does not appear in the search key then it should be a wrapper ( 's' appended )
            if ( !searchKeyMap.containsKey( currentMergeElement.getName() ) )
            {
                if ( !searchKeyMap.containsKey( currentMergeElement.getName().substring( 0, currentMergeElement.getName().length() - 1 ) ) )
                {
                    throw new XMLException( "Found unrecognised element '%1'. Is not a recognised entity or entity wrapper", currentMergeElement.getName() );
                }
            }

            // look for a main element that matches the merge element
            Element subMainElement = mainElement.element( currentMergeElement.getName() );

            // no element in the main doc so simply copy the whole element from the merge doc
            // into the main doc
            if ( subMainElement == null )
            {
                mainElement.content().add( currentMergeElement.createCopy() );
            }
            else
            {
                // check here to set if the REPLACE flag is set, if so then bin the main element and replace with the merge
                String actionValue = currentMergeElement.attributeValue( "Action" );

                if ( actionValue != null && actionValue.equalsIgnoreCase( "replace" ) )
                {
                    int index = mainElement.content().indexOf( subMainElement );

                    mainElement.content().set( index, currentMergeElement );
                }
                else
                {
                    for ( Iterator it2 = currentMergeElement.elementIterator(); it2.hasNext(); )
                    {
                        Element currentSubMergeElement = (Element)it2.next();

                        // build the search key map
                        List< String > currentElementSearchKeyMap = searchKeyMap.get( currentSubMergeElement.getName() );

                        if ( currentElementSearchKeyMap == null )
                        {
                            throw new XMLException( "Element '%1' did not have a search key specified", currentSubMergeElement.getName() );
                        }

                        // build the search map for this level
                        Map< String, String > currentSearchKeyMap = new HashMap< String, String >();

                        for ( String searchKey : currentElementSearchKeyMap )
                        {
                            // ignore search keys that aren't present eg CptId in Component isn't relevant for merging as the
                            // hierarchy structure of the file will ensure that the CptId is already matched up
                            if ( currentSubMergeElement.attribute( searchKey ) != null )
                            {
                                currentSearchKeyMap.put( searchKey, currentSubMergeElement.attribute( searchKey ).getValue() );
                            }
                        }

                        // search for the element with the specified attributes under the current main element ( non-recursive )
                        Element element = XMLHelper.findElementByAttributes( subMainElement, currentSearchKeyMap );

                        if ( element == null )
                        {
                            subMainElement.add( currentSubMergeElement.createCopy() );
                        }
                        else
                        {
                            // copy the merge attributes to the main sub-element
                            for ( Attribute attribute : (List< Attribute >)currentSubMergeElement.attributes() )
                            {
                                Attribute mainAttribute = element.attribute( attribute.getName() );
                                mainAttribute.setValue( attribute.getValue() );
                            }

                            // if merging the bottomost level
                            // and the main element has no sub elements replace the main element
                            // with the merge element
                            if ( level == 0 && element.elements().size() == 0 )
                            {
                                int index = subMainElement.content().indexOf( element );

                                // replace the old element with the new merge element
                                subMainElement.content().set( index, currentSubMergeElement.createCopy() );
                            }
                            else
                            {
                                // recursively merge the two together
                                doMergeLevel( element, currentSubMergeElement, level + 1 );
                            }
                        }
                    }
                }
            }
        }
    }
}
