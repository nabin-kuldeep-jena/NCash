package com.asjngroup.ncash.common.io.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class FileIOResourceHelper
{
	public static InputStream getResourceAsStream( String paramString )
	{
		ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
		InputStream localInputStream = null;
		String str = paramString;
		if ( str.startsWith( "/" ) )
			str = str.substring( 1 );
		try
		{
			if ( localClassLoader != null )
				localInputStream = localClassLoader.getResource( str ).openStream();
			if ( localInputStream == null )
				localInputStream = FileIOResourceHelper.class.getResource( paramString ).openStream();
			if ( localInputStream == null )
				localInputStream = FileIOResourceHelper.class.getClassLoader().getResource( str ).openStream();
		}
		catch ( IOException localIOException )
		{
			throw new NCashRuntimeException( localIOException );
		}
		return localInputStream;
	}

	public static URL getResourceURL( String paramString )
	{
		ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
		URL localURL = null;
		String str = paramString;
		if ( str.startsWith( "/" ) )
			str = str.substring( 1 );
		if ( localClassLoader != null )
			localURL = localClassLoader.getResource( str );
		if ( localURL == null )
			localURL = FileIOResourceHelper.class.getResource( paramString );
		if ( localURL == null )
			localURL = FileIOResourceHelper.class.getClassLoader().getResource( str );
		return localURL;
	}

	public static List<URL> getResourcesURLs( String paramString )
	{
		ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Enumeration localEnumeration = localClassLoader.getResources( paramString );
			return Collections.list( localEnumeration );
		}
		catch ( IOException localIOException )
		{
			throw new NCashRuntimeException( localIOException );
		}
	}

	public static void processResources( String paramString, InputHandler<URL, InputStream> paramInputHandler )
	{
		Iterator localIterator = getResourcesURLs( paramString ).iterator();
		while ( localIterator.hasNext() )
		{
			URL localURL = ( URL ) localIterator.next();
			InputStream localInputStream;
			try
			{
				localInputStream = localURL.openStream();
			}
			catch ( Exception localException1 )
			{
				throw new NCashRuntimeException( localException1 );
			}
			try
			{
				paramInputHandler.process( localURL, localInputStream );
			}
			catch ( Exception localException2 )
			{
				throw new NCashRuntimeException( "Error processing resource '%1'", localException2, new Object[1] );
			}
			finally
			{
				FileIOResourceHelper.closeSilent( localInputStream );
			}
		}
	}

	public static void processResources( List<String> paramList, InputHandler<URL, InputStream> paramInputHandler )
	{
		Iterator localIterator = paramList.iterator();
		while ( localIterator.hasNext() )
		{
			String str = ( String ) localIterator.next();
			processResources( str, paramInputHandler );
		}
	}

	public static boolean isResourceInArchive( URL paramURL )
	{
		String str;
		try
		{
			str = URLDecoder.decode( paramURL.getFile(), "UTF-8" );
		}
		catch ( UnsupportedEncodingException localUnsupportedEncodingException )
		{
			throw new NCashRuntimeException( localUnsupportedEncodingException );
		}
		return ( str.indexOf( "!" ) >= 0 );
	}

	public static void closeSilent( Closeable paramCloseable )
	{
		if ( paramCloseable == null )
			return;
		try
		{
			paramCloseable.close();
		}
		catch ( Exception localException )
		{
			localException.printStackTrace();
		}
	}
}