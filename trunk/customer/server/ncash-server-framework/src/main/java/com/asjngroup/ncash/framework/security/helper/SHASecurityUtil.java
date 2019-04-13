package com.asjngroup.ncash.framework.security.helper;

import org.apache.commons.codec.binary.Base64;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
* This class defines common routines for generating
* authentication signatures.
*/
public class SHASecurityUtil
{
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	public static String decryptSHA256( String data, String secret ) throws SignatureException
	{
		return calculateRFC2104HMAC( data, secret );
	}

	/**
	* Computes RFC 2104-compliant HMAC signature.
	* * @param data
	* The data to be signed.
	* @param key
	* The signing key.
	* @return
	* The Base64-encoded RFC 2104-compliant HMAC signature.
	* @throws
	* java.security.SignatureException when signature generation fails
	*/
	private static String calculateRFC2104HMAC( String data, String secret ) throws java.security.SignatureException
	{
		String result;
		try
		{

			// get an hmac_sha256 key from the raw secret bytes
			SecretKeySpec signingKey = new SecretKeySpec( secret.getBytes(), HMAC_SHA256_ALGORITHM );

			// get an hmac_sha256 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance( HMAC_SHA256_ALGORITHM );
			mac.init( signingKey );

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal( data.getBytes() );

			// base64-encode the hmac
			result = Base64.encodeBase64String( rawHmac ).toLowerCase();

		}
		catch ( Exception e )
		{
			throw new SignatureException( "Failed to generate HMAC : " + e.getMessage() );
		}
		return result;
	}
}
