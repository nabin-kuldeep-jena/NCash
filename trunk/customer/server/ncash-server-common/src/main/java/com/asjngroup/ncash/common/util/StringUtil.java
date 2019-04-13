package com.asjngroup.ncash.common.util;

/**
 * Any strings that require localisation (i.e. strings that will be displayed
 * to the user must be created via this class. Internally this class will
 * then return localised strings based on the string key passed in.
 * <p/>
 * The static StringResolver is used to actually perform the lookup between
 * key and localised string. If no StringResolver is specified then all
 * localised strings will default to the key.
 * <p/>
 * Additionally this class supports parameter substitution in strings, that
 * is placeholders (%1 to %n) can be replaced by parameter values.
 * <p/>
 * NOTE: this is class is SEALED because we use a scanning tool to extract
 * all the uses of this class for resource file generation! Do not change!
 *
 * @author nabin.jena
 */
public final class StringUtil
{
	private static StringResolver stringResolver = new StringResolver();


	/**
	 * Static method that should be used to create any string that should be
	 * localised.
	 *
	 * @param key  This is the message in the default language. This value
	 *             will be used as the key to look up the localised string. If no localised
	 *             string exists, or the StringResolver is not specified then the key will
	 *             be returned
	 * @param args Arguments that should replace the placeholders (%1 - %n) in
	 *             the string.
	 * @return The localised string, with all parameters subbed in
	 */
	
	public static void setStringResolver(StringResolver resolver) {
		 stringResolver = resolver;
	}
	public static String create( String key, Object... args )
	{
		if ( key == null || key.length() == 0 )
		{
			return key;
		}

		// If there are no args to sub in, assume the message string doesn't need any
		/// Get the localized string
		String message = resolve(key);

		// If there are no args to sub in, assume the message string doesn't
		// need any
		// and our work here is done
		if (args == null || args.length == 0) {
			return decodeUtf8(message);
		}

		// Now parse any placeholders in the localized string with the args
		// passed in
		message = parse(message, args);
		return decodeUtf8(message);
	}

	public static String decodeUtf8( String utftext )
	{
		String string = "";
		int i = 0;
		int c = 0;
		int c2 = 0, c3 = 0;

		while ( i < utftext.length() )
		{

			c = utftext.codePointAt( i );

			if ( c < 128 )
			{
				string += String.valueOf( ( ( char ) c ) );
				i++;
			}
			else if ( ( c > 191 ) && ( c < 224 ) )
			{
				c2 = utftext.codePointAt( i + 1 );

				string += String.valueOf( ( char ) ( ( ( c & 31 ) << 6 ) | ( c2 & 63 ) ) );
				i += 2;
			}
			else
			{
				c2 = utftext.codePointAt( i + 1 );
				c3 = utftext.codePointAt( i + 2 );
				string += String.valueOf( ( char ) ( ( ( c & 15 ) << 12 ) | ( ( c2 & 63 ) << 6 ) | ( c3 & 63 ) ) );
				i += 3;
			}

		}

		return string;
	}

	private static String parse( String baseMessage, Object[] args )
	{
		// Loop around all the parameters and replace the '%x' placedholders
		// with the corresponding entries in the args array (from %1 to %x)
		for ( int i = 0; i < args.length; i++ )
		{
			if ( args[i] == null )
			{
				baseMessage = StringHelper.replace( baseMessage, "%" + ( i + 1 ), "NULL" );
			}
			else
			{
				baseMessage = StringHelper.replace( baseMessage, "%" + ( i + 1 ), args[i].toString() );
			}
		}
		return baseMessage;
	}
	
	/**
	 * Returns a localized version of the SparkString. The localised version is
	 * determined by calling out to the StringResolver specified. If no
	 * StringResolver has been set then the key used to construct the
	 * SparkString will be returned.
	 * 
	 * @return The localised string, if the StringResolver is not null
	 */
	public static String resolve(String key) {
		 if ( stringResolver != null )
		 return stringResolver.resolveString( key );
		return key;
	}

}
