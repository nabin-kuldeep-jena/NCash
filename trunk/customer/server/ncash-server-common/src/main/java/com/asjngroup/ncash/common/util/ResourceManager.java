package com.asjngroup.ncash.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceManager
{
	private static String[] I18NPropFiles;
	private static final Log log = LogFactory.getLog( ResourceManager.class );
	private static ResourceManagerListener listener;
	private static final HashMap<String, Locale> cache = new HashMap();
	private static String resourcePoolName = null;

	public static void intialise( String fileNames, ResourceManagerListener rMListener )
	{
		if ( !( StringHelper.isEmpty( fileNames ) ) )
			I18NPropFiles = fileNames.split( "," );
		listener = rMListener;
	}

	public static String getI18NString( String prefix, String key )
	{
		if ( ( I18NPropFiles == null ) || ( I18NPropFiles.length == 0 ) )
		{
			log.debug( "No resource bundle specified in web.xml" );
			return key;
		}
		ResourceBundle resourceBundle = null;
		for ( int i = 0; i < I18NPropFiles.length; ++i )
		{
			String propertyFileName = I18NPropFiles[i].trim();
			try
			{
				Locale locale = getLocale( listener.getLocalInfo() );
				resourceBundle = ResourceBundle.getBundle( propertyFileName, locale );
				if ( resourceBundle == null )
				{
					log.debug( "can't find resource bundle " + propertyFileName );
					return key;
				}
				return resourceBundle.getString( prefix + "." + key );
			}
			catch ( MissingResourceException e )
			{
				log.debug( "can't find resource for bundle " + propertyFileName + ", key " + prefix + "." + key );
			}
		}

		return key;
	}

	private static Locale getLocale( String localeInfo )
	{
		if ( StringHelper.isEmpty( localeInfo ) )
			return Locale.ENGLISH;
		if ( cache.containsKey( localeInfo ) )
			return ( ( Locale ) cache.get( localeInfo ) );
		Locale locale = new Locale( localeInfo );
		cache.put( localeInfo, locale );
		return locale;
	}

	public static String getResourcePoolName()
	{
		return resourcePoolName;
	}
}