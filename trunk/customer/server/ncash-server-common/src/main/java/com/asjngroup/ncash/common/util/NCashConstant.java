package com.asjngroup.ncash.common.util;

import org.joda.time.DateTime;

import java.util.Date;

public class NCashConstant
{
	// timezone stuff sorted
	public static final DateTime NULL_DATE_TIME = new DateTime( 9999, 1, 1, 23, 59, 59, 0 );
	public static final DateTime MAX_DATE_TIME = new DateTime( 9999, 1, 1, 23, 59, 59, 0 );

	public static final Date NULL_DATETIME = new Date( -789, 1, 1, 1, 1, 1 );//Jan 01 1111 01:01:01
	public static final Date MIN_DATETIME = new Date( 0, 0, 1, 1, 1, 1 );//Jan 01 1900 01:01:01
	public static final Date MAX_DATETIME = new Date( 8099, 0, 1, 23, 59, 59 );//Jan 01 9999 23:59:59

	public static final String DEFAULT_CHARSET = "UTF-8";
	public static final String DEFAULT_CONTEXT = "Default";
	public static final String ADMINISTRATOR_USER = "Administrator";
	public static final String DEFAULT_USER = "NCash";
	public static final String DEFAULT_ROLE = "NCashAdmin";
	public static final String USER_SESSION = "USER_SESSION";
}
