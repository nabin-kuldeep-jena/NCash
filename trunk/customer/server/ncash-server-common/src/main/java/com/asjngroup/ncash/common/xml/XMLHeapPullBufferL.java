package com.asjngroup.ncash.common.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.*;

import com.asjngroup.ncash.common.BinaryHelper;
import com.asjngroup.ncash.common.buffer.ByteBufferL;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;


public class XMLHeapPullBufferL extends ByteBufferL
{
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private boolean isField = false;
    private boolean moveNext = false;
    private boolean executedOnce = false;
    private boolean isAttribParser = false;
    private boolean isElementPresent = true;
    private boolean recordChanged = false;

    private String fieldValue = "";
    private String fieldName = "";
    private String currentElementName = "";

    private int attributeCount = 0;

    private XMLStreamReader reader = null;
    private FileInputStream fisGlobal = null;

    private Map<String,String> attributes = new HashMap<String, String>();

    public XMLHeapPullBufferL( String recordType, File file, File tempFile, FileInputStream fisGlobal, long position )
    {
    this (  recordType, null,  file,  tempFile,  fisGlobal,  position  );
    }

    public XMLHeapPullBufferL( String recordType, InputStream inputStream, File tempFile, FileInputStream fisGlobal, long position )
    {
    	this (  recordType, inputStream, null,  tempFile,  fisGlobal,  position  );
    }

    private XMLHeapPullBufferL( String recordType, InputStream inputStream, File file, File tempFile, FileInputStream fisGlobal, long position )
    {
		super( -1, 0, 0, 0 );
        this.fisGlobal = fisGlobal;

        FileInputStream fis = null;
        FileOutputStream fos = null;
		BufferedReader bufferedReader = null;
        byte[] buffer = null;
        try
        {
            if( inputStream!=null )
        	{
        		XmlCompressionInputStream xmlCompressionInputStream = new XmlCompressionInputStream( inputStream, true );
        		buffer = new byte[2048];
        		xmlCompressionInputStream.read( buffer, 0, buffer.length );
        	}
        	else
        	{
	            fis = new XmlFileInputStream( file, true );
	            buffer = new byte[2048];
	            fis.read( buffer, 0, buffer.length );
        	}

        	fos = new FileOutputStream( tempFile );
            String xmlString = BinaryHelper.getString( buffer, 0, buffer.length ).trim();
            String endTag = getRootTagName( xmlString );

            fos.write( xmlString.getBytes() );
			if(fis!=null)
            {
            	fis.close();
            }



            if ( inputStream!=null )
            {
            	inputStream.skip( position );
                bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
            }
            else
            {
            	fis = new FileInputStream( file );
            	fis.skip( position );
                bufferedReader = new BufferedReader( new InputStreamReader( fis ) );
            }

            //fis.skip( position );
            //BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( fis ) );
            boolean loop = true;

            String terminatingTag = ("</" + recordType + ">").toLowerCase();
            String selfTerminatingTag = "/>";
            StringBuffer xmlText = new StringBuffer( DEFAULT_BUFFER_SIZE );
            while ( loop )
            {
                String line = bufferedReader.readLine();
                String outputLine = line.trim();
                line = line.trim().toLowerCase();
                if ( line.contains( terminatingTag ))
                {
                    int index = line.indexOf( terminatingTag );
                    line = outputLine.substring( 0, (line.indexOf( ">", index + 1 ) + 1) );
                    outputLine = line;
                    loop = false;
                }
                else if ( line.contains( selfTerminatingTag ))
                {
                    int index = line.indexOf( selfTerminatingTag );
                    line = outputLine.substring( 0, (line.indexOf( ">", index + 1 ) + 1) );
                    outputLine = line;
                    loop = false;
                }

                if ( ( xmlText.length() + line.length() ) >= DEFAULT_BUFFER_SIZE )
                {
                    fos.write( xmlText.toString().getBytes() );
                    xmlText.delete( 0, xmlText.length() );
                }
                xmlText.append( outputLine );
            }

            xmlText.append( endTag );
            fos.write( xmlText.toString().getBytes() );
            bufferedReader.close();
            fos.close();

            capacity( tempFile.length() );
            limit( tempFile.length() );
            initialise();
        }
        catch ( FileNotFoundException e )
        {
            throw new NCashRuntimeException( "Input file not found." + e );
        }
        catch ( IOException e )
        {
            throw new NCashRuntimeException( "Unable to read from the specified file." + e );
        }
        finally
        {
            try
            {
                if ( fis != null )
                    fis.close();

                if ( fos != null )
                    fos.close();

				if( inputStream != null )
                	inputStream.close();
            }
            catch ( IOException e )
            {
                throw new NCashRuntimeException( "Unable to close specified file." + e );
            }
        }
    }

    public String getRootTagName(String xmlString)
    {
        int fromIndex = 0;
        while(true)
        {
            int index = xmlString.indexOf( "<", fromIndex );
            fromIndex = index + 1;
            String ch = String.valueOf( xmlString.charAt( index + 1 ));
            if (ch.matches( "[a-zA-Z_]" ))
            {
                index = xmlString.indexOf( ">", fromIndex );
                String tagName = xmlString.substring( fromIndex - 1, index + 1 );
                if (tagName.contains( " " ))
                    tagName = tagName.split( " " )[0] + ">";
                return tagName;
            }
        }
    }

    public void initialise()
    {
        try
        {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            reader = factory.createXMLStreamReader( fisGlobal );
            moveToRootElement();

            if (attributeCount == 0)
            {
            	moveNext = true;
                moveToNextElement();
                moveNext = false;
            }
        }
        catch ( XMLStreamException e )
        {
            throw new NCashRuntimeException( e );
        }
        /*catch ( FileNotFoundException e )
        {
            throw new SparkRuntimeException( "Input file " + file.getName() + " not found." + e );
        }*/
    }

    public boolean hasElements( String elementName, int depthLevel )
    {
        try
        {
            if ( !executedOnce )
            {
                while ( reader.hasNext() )
                {
                    String recordName = null;
                    if ( reader.getPrefix() == null )
                        recordName = reader.getLocalName();
                    else
                        recordName = reader.getPrefix() + ":" + reader.getLocalName();
                    if ( recordName.equalsIgnoreCase( elementName ) )
                    {
                    	attributeCount = reader.getAttributeCount();
                    	if (attributeCount > 0)
                    	{
                    		constructAttributeList(attributeCount, true);
                    		moveNext = false;
                    	}
                    	else
                    		moveNext = true;

                        executedOnce = true;
                        return true;
                    }

                    if ( reader.hasNext() )
                        reader.next();
                }
                executedOnce = true;
            }
            else
            {
                if ( ( reader.hasNext() ) )
                {
                    String recordName = null;
                    if ( reader.getPrefix() == null )
                        recordName = reader.getLocalName();
                    else
                        recordName = reader.getPrefix() + ":" + reader.getLocalName();
                    if ( recordName.equalsIgnoreCase( elementName ) )
                    {
                    	attributeCount = reader.getAttributeCount();
                    	if (attributeCount > 0)
                    	{
                    		constructAttributeList(attributeCount, true);
                    		moveNext = false;
                    	}
                    	else
                    		moveNext = true;
                        return true;
                    }
                }
            }
        }
        catch ( XMLStreamException e )
        {
            throw new NCashRuntimeException( e );
        }

        moveNext = false;
        return false;
    }

    public void setRecordChanged(boolean changed)
    {
    	this.recordChanged = changed;
    }

    public String getFieldValue( String key )
    {
        try
        {
        	fieldValue = null;
        	isElementPresent = true;

        	if (attributeCount > 0 && isAttribParser)
        		processAttributeValue(key);
        	else
        		processFieldValue( key );
        }
        catch ( XMLStreamException e )
        {
            return null;
        }

        return fieldValue;
    }

    public boolean isCountNotValid(String elementName)
    {
    	boolean returnValue = false;

    	if (!recordChanged)
    		returnValue = (attributeCount > 0 && elementName.equalsIgnoreCase(currentElementName))? true : false;

    	return returnValue;
    }

    public void setParserType(boolean isAttrib)
    {
    	isAttribParser = isAttrib;
    }

    public boolean getIsElementPresent()
    {
    	return this.isElementPresent;
    }

    public long getOffset( String key )
    {
        return 0L;
    }

    public int getLength( String key )
    {
        return 0;
    }

    public void processAttributeValue( String key)
    {
    	if (!recordChanged)
    	{
			if (attributes.containsKey(key.toLowerCase()))
			{
				attributeCount--;
				fieldValue = attributes.get(key.toLowerCase());
				attributes.remove(key.toLowerCase());
			}
			else
				isElementPresent = false;

			if (attributeCount <=0)
			{
				attributes.clear();
				moveToNext();
			}
		}
    	else
    		isElementPresent = false;
    }

    public void processFieldValue( String key ) throws XMLStreamException
    {
        if ( !reader.hasNext() )
            return;

        boolean loop = true;
        while ( loop )
        {
            switch (reader.getEventType())
            {
            case XMLStreamConstants.START_ELEMENT:
            {
                if ( reader.getPrefix() == null )
                    fieldName = reader.getLocalName().toLowerCase();
                else
                    fieldName = ( reader.getPrefix() + ":" + reader.getLocalName() ).toLowerCase();
                if ( !fieldName.equalsIgnoreCase( key ) )
                {
                    moveNext = false;
                    isElementPresent = false;
                    return;
                }
            }
                break;
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.SPACE:
            {
                String text = processText( reader.getText() );
                if ( isField )
                    fieldValue = text;
            }
                break;
            case XMLStreamConstants.END_ELEMENT:
            {
                loop = false;
            }
                break;
            }

            try
            {
                if ( reader.hasNext() && loop )
                    reader.next();
            }
            catch ( XMLStreamException e )
            {
                if ( e.getMessage().toLowerCase().contains( "must be terminated by the matching end-tag" ) )
                    loop = false;
                else
                    throw e;
            }
        }
        moveNext = true;
        moveToNextElement();
        moveNext = false;
    }

    public void moveToNextElement()
    {
        try
        {
            if ( !moveNext )
                return;

            if ( !reader.hasNext() )
                return;

            boolean loop = true;

            reader.next();
            while ( loop )
            {
                switch (reader.getEventType())
                {
                case XMLStreamConstants.END_DOCUMENT:
                    loop = false;
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    loop = false;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                {
                    int length = reader.getLocation().getCharacterOffset();
                    if ( length <= limit() )
                        position( length );
                    else
                        position( limit() );
                }
                    break;
                }

                if ( reader.hasNext() && loop )
                    reader.next();
            }
        }
        catch ( XMLStreamException e )
        {
            if ( e.getMessage().toLowerCase().contains( "must be terminated by the matching end-tag" ) )
            {
            	isElementPresent = false;
                moveNext = true;
                moveToNextElement();
                moveNext = false;
            }
            else
                throw new NCashRuntimeException( "Trying to move next but found empty buffer." + e );
        }
    }

    public void moveToRootElement()
    {
        try
        {
            if ( !reader.hasNext() )
                return;

            boolean loop = true;

            while ( loop )
            {
                switch (reader.getEventType())
                {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    loop = false;
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    loop = false;
                    break;
                }

                if ( reader.hasNext() && loop )
                    reader.next();
            }

            moveNext = true;
            moveToNext();
            moveNext = false;
        }
        catch ( XMLStreamException e )
        {
            throw new NCashRuntimeException( "Trying to move next but found empty buffer." + e );
        }
    }

    private void moveToNext()
    {
    	moveNext = true;
		moveToNextElement();
		try
		{
			if (reader.hasNext())
			{
				attributeCount = reader.getAttributeCount();
				if (attributeCount > 0)
					constructAttributeList(attributeCount, true);
			}
		}
		catch (XMLStreamException e)
		{
			throw new NCashRuntimeException(e);
		}
        moveNext = false;
	}

    private void constructAttributeList(int count, boolean changed)
    {
    	attributes.clear();
    	recordChanged = changed;

    	if ( reader.getPrefix() == null )
    		currentElementName = reader.getLocalName().toLowerCase();
        else
        	currentElementName = ( reader.getPrefix() + ":" + reader.getLocalName() ).toLowerCase();

    	for(int i=0;i<count;i++)
    	{
    		String key = "";
    		String value = "";

    		if ( (reader.getAttributePrefix(i) == null) || ("".equals(reader.getAttributePrefix(i))))
    			key = reader.getAttributeLocalName(i);
            else
            	key = ( reader.getAttributePrefix(i) + ":" + reader.getAttributeLocalName(i) ).toLowerCase();

        	value = reader.getAttributeValue(i);

        	attributes.put(key.toLowerCase(), value);
    	}
    }

    private String processText( String text )
    {
        char[] ch = text.toCharArray();
        String returnText = "";
        boolean textPresent = false;

        for ( int i = 0; i < text.length(); i++ )
        {
            switch (ch[ i ])
            {
            case '\\':
            case '"':
            case '\n':
            case '\r':
            case '\t':
                break;
            default:
                returnText = returnText.concat( String.valueOf( ch[ i ] ) );
                textPresent = true;
                break;
            }
        }
        if ( textPresent )
            isField = true;
        else
            isField = false;

        return returnText;
    }

    public ByteBufferL compact()
    {
        return null;
    }

    public ByteBufferL duplicate()
    {
        return null;
    }

    public byte get()
    {
        return 0;
    }

    public byte get( long index )
    {
        return 0;
    }

    public ByteBufferL put( byte b )
    {
        return null;
    }

    public ByteBufferL put( long index, byte b )
    {
        return null;
    }

    public ByteBufferL slice()
    {
        return null;
    }

    public ByteBuffer toByteBuffer( int length )
    {
        return null;
    }

    public boolean isReadOnly()
    {
        return true;
    }

	private class XmlCompressionInputStream
    {
        public static final String TAG_START_COLON_PATTERN = "<[A-Za-z0-9_]*[:][A-Za-z0-9_]*";
        public static final String TAG_END_COLON_PATTERN = "</[A-Za-z0-9_]*[:][A-Za-z0-9_]*";
        public static final String TAG_START_END_COLON_PATTERN = "<[A-Za-z0-9_]*[:][A-Za-z0-9_]*/";

        public static final String TAG_START_PATTERN = "<[A-Za-z0-9_]*";
        public static final String TAG_END_PATTERN = "</[A-Za-z0-9_]*";
        public static final String TAG_START_END_PATTERN = "<[A-Za-z0-9_]*/";

        private InputStream inputStream;
        private boolean finished = false;
        private boolean readFirstTag = false;
        private String elementName = "";

        public XmlCompressionInputStream( InputStream inputStream, boolean readFirstTag ) throws FileNotFoundException
        {
            this.readFirstTag = readFirstTag;
            this.inputStream = inputStream;
        }

        public int read( byte b[], int off, int len ) throws IOException
        {
            if ( finished )
                return -1;
            int totalBytesRead = 0;

            for ( int i = off; i < len; i++ )
            {
                int byteValue = inputStream.read();
                if ( byteValue == -1 )
                {
                    finished = true;
                    break;
                }

                char ch = (char)byteValue;
                totalBytesRead++;
                b[ i ] = (byte)ch;

                switch (ch)
                {
                case '<':
                {
                    elementName = "";
                    elementName += ch;
                }
                    break;
                case '>':
                {
                    if (elementName.contains( " " ))
                        elementName = elementName.split( " " )[0];

                    if ( elementName.matches( TAG_START_COLON_PATTERN ) || elementName.matches( TAG_START_END_COLON_PATTERN ) || elementName.matches( TAG_START_PATTERN ) || elementName.matches( TAG_START_END_PATTERN ) )
                    {
                        elementName = "";
                        if (readFirstTag)
                            i = len;
                    }
                }
                    break;
                case '\n':
                case '\r':
                case '\t':
                    break;
                default:
                    elementName += ch;
                }
            }
            return totalBytesRead;
        }
    }


    //  Helper Stream class to get the XML tag's offset and length
    private class XmlFileInputStream extends FileInputStream
    {
        public static final String TAG_START_COLON_PATTERN = "<[A-Za-z0-9_]*[:][A-Za-z0-9_]*";
        public static final String TAG_END_COLON_PATTERN = "</[A-Za-z0-9_]*[:][A-Za-z0-9_]*";
        public static final String TAG_START_END_COLON_PATTERN = "<[A-Za-z0-9_]*[:][A-Za-z0-9_]*/";

        public static final String TAG_START_PATTERN = "<[A-Za-z0-9_]*";
        public static final String TAG_END_PATTERN = "</[A-Za-z0-9_]*";
        public static final String TAG_START_END_PATTERN = "<[A-Za-z0-9_]*/";

        private boolean finished = false;
        private boolean readFirstTag = false;
        private String elementName = "";

        public XmlFileInputStream( File file, boolean readFirstTag ) throws FileNotFoundException
        {
            super( file );
            this.readFirstTag = readFirstTag;
        }

        public int read( byte b[], int off, int len ) throws IOException
        {
            if ( finished )
                return -1;
            int totalBytesRead = 0;

            for ( int i = off; i < len; i++ )
            {
                int byteValue = read();
                if ( byteValue == -1 )
                {
                    finished = true;
                    break;
                }

                char ch = (char)byteValue;
                totalBytesRead++;
                b[ i ] = (byte)ch;

                switch (ch)
                {
                case '<':
                {
                    elementName = "";
                    elementName += ch;
                }
                    break;
                case '>':
                {
                    if (elementName.contains( " " ))
                        elementName = elementName.split( " " )[0];

                    if ( elementName.matches( TAG_START_COLON_PATTERN ) || elementName.matches( TAG_START_END_COLON_PATTERN ) || elementName.matches( TAG_START_PATTERN ) || elementName.matches( TAG_START_END_PATTERN ) )
                    {
                        elementName = "";
                        if (readFirstTag)
                            i = len;
                    }
                }
                    break;
                case '\n':
                case '\r':
                case '\t':
                    break;
                default:
                    elementName += ch;
                }
            }
            return totalBytesRead;
        }
    }
}
