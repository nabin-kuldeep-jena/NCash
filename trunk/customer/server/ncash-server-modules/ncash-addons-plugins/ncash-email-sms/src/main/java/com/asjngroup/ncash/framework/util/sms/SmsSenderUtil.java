package com.asjngroup.ncash.framework.util.sms;

import java.util.List;

public class SmsSenderUtil
{
	private static SMSSender smsSenderInstance;

	private SmsSenderUtil()
	{

	}

	public static SmsSenderUtil getInstance( SMSSender smsSender )
	{
		smsSenderInstance = smsSender;
		return null;
	}

	public static String sendPromotionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException
	{

		return smsSenderInstance.sendPromotionalSms( mobileNoList, countryCode, message, senderId );

	}

	public static String sendTransactionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return smsSenderInstance.sendTransactionalSms( mobileNoList, countryCode, message, senderId );
	}

	public static String sendOtpSms( String mobileno, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return smsSenderInstance.sendOtpSms( mobileno, countryCode, message, senderId );
	}

	public static String sendPromotionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return smsSenderInstance.sendPromotionalSms( mobileNo, countryCode, message, senderId );
	}

	public static String sendTransactionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException
	{
		return smsSenderInstance.sendTransactionalSms( mobileNo, countryCode, message, senderId );
	}

}
