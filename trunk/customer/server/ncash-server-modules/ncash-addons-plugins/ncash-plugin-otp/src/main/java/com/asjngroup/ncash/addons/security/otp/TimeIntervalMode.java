package com.asjngroup.ncash.addons.security.otp;

public enum TimeIntervalMode
{

	TENSEC( 10 ), THIRTYSEC( 30 ), ONEMIN( 60 ), FIVEMIN( 300 ), TENMIN(
			600 ), THIRTYMIN( 1800 ), ONEHOUR(
					3600 ), TWELVEHOUR( 43200 ), ONEDAY( 86400 );

	public int timeDuration;

	private TimeIntervalMode( int timeDuration )
	{
		this.timeDuration = timeDuration;
	}

	public int getTimeDuration()
	{
		return timeDuration;
	}
}
