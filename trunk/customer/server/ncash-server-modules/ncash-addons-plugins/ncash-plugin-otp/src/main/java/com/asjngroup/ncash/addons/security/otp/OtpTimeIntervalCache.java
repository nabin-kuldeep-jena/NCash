package com.asjngroup.ncash.addons.security.otp;

import org.jboss.aerogear.security.otp.api.Clock;

import java.util.HashMap;

public class OtpTimeIntervalCache
{
	private static HashMap<TimeIntervalMode, Clock> mapCache = new HashMap<TimeIntervalMode, Clock>();

	static
	{
		for ( TimeIntervalMode mode : TimeIntervalMode.values() )
		{
			mapCache.put( mode, new Clock( mode.getTimeDuration() ) );
		}

	}

	public static Clock getTimeInterval( TimeIntervalMode timeIntervalMode )
	{
		return mapCache.get( timeIntervalMode );
	}
}
