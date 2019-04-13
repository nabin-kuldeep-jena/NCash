package com.asjngroup.ncash.framework.util.sms;

import java.util.List;

public interface SMSSender
{

	public String sendPromotionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException;

	public String sendPromotionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException;

	public String sendTransactionalSms( List<String> mobileNoList, String countryCode, String message, String senderId ) throws SMSSendingException;

	public String sendTransactionalSms( String mobileNo, String countryCode, String message, String senderId ) throws SMSSendingException;

	public String sendOtpSms( String mobileno, String countryCode, String message, String senderId ) throws SMSSendingException;
}
