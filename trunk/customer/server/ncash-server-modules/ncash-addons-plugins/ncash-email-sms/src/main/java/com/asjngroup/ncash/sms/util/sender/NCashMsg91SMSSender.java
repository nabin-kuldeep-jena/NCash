package com.asjngroup.ncash.sms.util.sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.framework.util.sms.SMSSender;
import com.asjngroup.ncash.framework.util.sms.SMSSendingException;

public class NCashMsg91SMSSender implements SMSSender
{
	private String authKey;
	private String gatewayUrl;
	private String promotionalRouteId = "1";
	private String otpRouteId = "4";
	private String transactionalRouteId = "4";

	public NCashMsg91SMSSender( String authKey, String gatewayUrl )
	{
		this.authKey = authKey;
		this.gatewayUrl = gatewayUrl;
	}

	public String sendPromotionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException
	{

		return sendSms( mobileNoList, null, message, senderId, promotionalRouteId );

	}

	private String sendSms( List<String> mobileNoList, String mobileNo, String message, String senderId, String route ) throws SMSSendingException
	{
		URLConnection smsUrlConnection = null;
		URL smsURL = null;
		BufferedReader reader = null;

		String encoded_message = URLEncoder.encode( message );

		//Prepare parameter string 
		StringBuilder smsUrlString = new StringBuilder( gatewayUrl );
		smsUrlString.append( "?authkey=" + authKey );
		if ( mobileNoList != null && !mobileNoList.isEmpty() )
			smsUrlString.append( "&mobiles=" + StringHelper.merge( mobileNoList, "," ) );
		else
			smsUrlString.append( "&mobiles=" + mobileNo );
		smsUrlString.append( "&message=" + encoded_message );
		smsUrlString.append( "&route=" + route );
		smsUrlString.append( "&sender=" + senderId );

		try
		{
			//prepare connection
			smsURL = new URL( smsUrlString.toString() );
			smsUrlConnection = smsURL.openConnection();
			smsUrlConnection.connect();
			reader = new BufferedReader( new InputStreamReader( smsUrlConnection.getInputStream() ) );
			//reading response 
			String response;
			while ( ( response = reader.readLine() ) != null )
				System.out.println( response );
			return response;
		}
		catch ( IOException e )
		{
			throw new SMSSendingException( e );
		}
		finally
		{
			if ( reader != null )
				try
				{
					reader.close();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
		}
	}

	public String sendTransactionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return sendSms( mobileNoList, null, message, senderId, transactionalRouteId );
	}

	public String sendOtpSms( String mobileno, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return sendSms( null, mobileno, message, senderId, otpRouteId );
	}

	public String sendPromotionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return sendSms( null, mobileNo, message, senderId, promotionalRouteId );
	}

	public String sendTransactionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return sendSms( null, mobileNo, message, senderId, transactionalRouteId );
	}
}
