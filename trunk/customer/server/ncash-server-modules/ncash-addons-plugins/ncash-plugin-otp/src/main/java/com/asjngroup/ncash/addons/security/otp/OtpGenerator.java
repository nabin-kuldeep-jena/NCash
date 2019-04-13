package com.asjngroup.ncash.addons.security.otp;

import org.jboss.aerogear.security.otp.Totp;

public class OtpGenerator {

	public static String generateTimeBasedOtp(String userName, String clientIdCode, TimeIntervalMode intervalMode) {
		Totp totp = new Totp(clientIdCode, OtpTimeIntervalCache.getTimeInterval(intervalMode));
		return totp.now();
	}

	public static String generateBasedOtp(String userName, String clientIdCode) {
		Totp totp = new Totp(clientIdCode);
		return totp.now();
	}

	public static boolean verifyTimeBased(String userName, String clientIdCode, String otp,
			TimeIntervalMode timeIntervalMode) {
		Totp totp = new Totp(clientIdCode, OtpTimeIntervalCache.getTimeInterval(timeIntervalMode));
		return totp.verify(otp);
	}

	public static boolean verify(String userName, String clientIdCode, String otp) {
		Totp totp = new Totp(clientIdCode);
		return totp.verify(otp);
	}

}
