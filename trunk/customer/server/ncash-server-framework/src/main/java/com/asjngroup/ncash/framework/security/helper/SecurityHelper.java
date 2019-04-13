package com.asjngroup.ncash.framework.security.helper;

import java.security.MessageDigest;

import com.asjngroup.ncash.common.BinaryHelper;

public class SecurityHelper
{
	public static String hashPassword( String unhashedPassword )
	{
		if ( unhashedPassword == null )
		{
			return null;
		}

		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance( "SHA-512" );
			messageDigest.update( unhashedPassword.getBytes( "UTF-16BE" ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

		return BinaryHelper.byteArrayToHexString( messageDigest.digest() );
	}

	public static String hashPassword( String unhashedPassword, String algThm )
	{
		if ( unhashedPassword == null )
		{
			return null;
		}

		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance( algThm );
			messageDigest.update( unhashedPassword.getBytes( "UTF-16BE" ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

		return BinaryHelper.byteArrayToHexString( messageDigest.digest() );
	}
}
