package com.asjngroup.ncash.common.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper
{

	static
	{
		// stash the actual system joda timezone
		systemTimezone = DateTimeZone.getDefault();
	}

	private static final DateTimeZone systemTimezone;

	public static final String dateFormatStringForStorage = "yyyy-MM-dd HH:mm:ss";
	public static final String dateFormatStringForMilliStorage = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	public static final String dateFormatStringForOracleStorage = "yyyy-MM-dd HH24:mi:ss";
	public static final String dateFormatStringForDB2Storage = "yyyy-MM-dd HH:mm:ss";
	public static DateTime minDate = new DateTime( 1900, 1, 1, 1, 1, 1, 0, systemTimezone );
	public static DateTime maxDate = new DateTime( 9999, 1, 1, 23, 59, 59, 0, systemTimezone );
	public static final DateTime nullDate = new DateTime( 1111, 1, 1, 1, 1, 1, 0 );

	public static DateTime getMinimumDate()
	{
		return minDate;
	}

	public static DateTime getMaximumDate()
	{
		return maxDate;
	}

	/*
		Use getDateByCalendar(Date,DateFormatter) to get the date time as a string.
		 This will do all the neceassary checks for loacalisation of calendars and returns time string according to user calendar.
	 */

	public static final String dateFormatStringForVerticaStorage = "yyyy-MM-dd HH24:mi:ss";
	public static final String dayMthYrFmt = "dd/MM/yy HH:mm:ss";
	public static final DateTimeFormatter mmddyyyyhhmmssFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy HH:mm:ss" );
	public static final DateTimeFormatter dateStringForStorageFormatter = DateTimeFormat.forPattern( dateFormatStringForStorage );
	public static final DateTimeFormatter dateStringForStorageMilliFormatter = DateTimeFormat.forPattern( dateFormatStringForMilliStorage );
	public static final DateTimeFormatter yyyymmddhhmmssFormatter = DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss" );
	public static final DateTimeFormatter yyyymmddhhmmssSSSSSSFormatter = DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss.SSSSSS" );
	public static final DateTimeFormatter yyyymmddhhmmssContinuousFormatter = DateTimeFormat.forPattern( "yyyyMMddHHmmss" );
	public static final DateTimeFormatter yyyymmddFormatter = DateTimeFormat.forPattern( "yyyyMMdd" );
	public static final DateTimeFormatter hhmmssFormatter = DateTimeFormat.forPattern( "HH:mm:ss" );
	public static final DateTimeFormatter mmddyyyyWithSlashFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy" );
	public static final DateTimeFormatter ddMMyyyyFormatterForLookup = DateTimeFormat.forPattern( "yyyy-MM-dd" );
	public static final DateTimeFormatter ddMMyyyyFormatter = DateTimeFormat.forPattern( "dd/MM/yyyy" );
	public static final DateTimeFormatter ddMMyyyyHHmmssFormatter = DateTimeFormat.forPattern( "dd/MM/yyyy HH:mm:ss" );

	public static final DateTimeFormatter tariffTimeLineFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy" );
	public static final DateTimeFormatter yyyymmddhhmmssStorageFormatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" );
	public static final DateTimeFormatter dayMthYrFmtFormatter = DateTimeFormat.forPattern( dayMthYrFmt );

	public static DateTimeZone getSystemtimezone()
	{
		return systemTimezone;
	}

	static class TimeZoneOffsetProvider implements DateTimeUtils.MillisProvider
	{
		private DateTimeZone dateTimeZone;

		TimeZoneOffsetProvider( DateTimeZone dateTimeZone )
		{
			this.dateTimeZone = dateTimeZone;
		}

		public long getMillis()
		{
			long instant = System.currentTimeMillis();

			long millis = instant + this.dateTimeZone.getOffset( instant );

			return millis;
		}
	}

	public static void setDefaultUTCTimeZone()
	{
		// set the millis generator to generate UTC'd wallclock time
		// This was a customized function in v1.2.1. But when we upgraded joda-time 2.3 jar had support
		DateTimeUtils.setCurrentMillisProvider( new TimeZoneOffsetProvider( systemTimezone ) );

		// set default timezones for joda and java
		setGlobalJodaTimeZone( DateTimeZone.UTC );
		setGlobalJavaTimeZone( DateTimeZone.UTC.toTimeZone() );
	}

	public static void setGlobalJodaTimeZone( DateTimeZone dateTimeZone )
	{
		// default the system joda timezone and java time zone
		DateTimeZone.setDefault( dateTimeZone );
		minDate = minDate.withZoneRetainFields( dateTimeZone );
		maxDate = maxDate.withZoneRetainFields( dateTimeZone );
	}

	public static void setGlobalJodaAndJavaTimeZone( String dateTimeZoneId )
	{
		// default the system joda timezone and java time zone
		DateTimeZone dateTimeZone = DateTimeZone.forID( dateTimeZoneId );
		DateTimeZone.setDefault( dateTimeZone );
		minDate = minDate.withZoneRetainFields( dateTimeZone );
		maxDate = maxDate.withZoneRetainFields( dateTimeZone );
		TimeZone.setDefault( dateTimeZone.toTimeZone() );
	}

	public static void setGlobalJavaTimeZone( TimeZone timeZone )
	{
		// default the system joda timezone
		TimeZone.setDefault( timeZone );
	}

	public static DateTime addMillis( DateTime date, long amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.add( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addYears( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addYears( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addMonths( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addMonths( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addWeeks( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addWeeks( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addHours( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addHours( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addDays( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addDays( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addMinutes( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addMinutes( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addSeconds( DateTime date, int amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.addSeconds( amount );
		return newDateTime.toDateTime();
	}

	public static DateTime addSeconds( DateTime date, long amount )
	{
		MutableDateTime newDateTime = date.toMutableDateTime();
		newDateTime.add( amount * 1000 );
		return newDateTime.toDateTime();
	}

	// diff functions ( does date2 - earlyDate and returns the difference )
	public static int yearDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.yearOfEra().getDifference( earlyDate );
	}

	public static int monthDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.monthOfYear().getDifference( earlyDate );
	}

	public static int weekDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.weekOfWeekyear().getDifference( earlyDate );
	}

	public static int dayDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.dayOfYear().getDifference( earlyDate );
	}

	public static long secondDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.secondOfDay().getDifferenceAsLong( earlyDate );
	}

	public static long milliDiff( DateTime earlyDate, DateTime lateDate )
	{
		return lateDate.millisOfDay().getDifferenceAsLong( earlyDate );
	}

	// returns true if two date-times are within (symmetrically) a certain threshold value of each other

	public static boolean isWithinTimeThresholdSecond( DateTime earlierDate, DateTime laterDate, int threshold )
	{
		return Math.abs( DateHelper.secondDiff( earlierDate, laterDate ) ) <= threshold;
	}

	/*public static boolean isEmptyDate( DateTime dateTime )
	{
		// check for the NULL_DATETIME constant
		if ( dateTime.equals( nullDate ) )
		{
			return true;
		}
	
		// check for either the MIN_DATETIME or MAX_DATETIME constants
		if ( dateTime.equals( minDate ) || dateTime.equals( maxDate ) )
		{
			return true;
		}
	
		// if we get here the date is not empty
		return false;
	}*/

	public static boolean isWithinTimeThresholdMilli( DateTime earlierDate, DateTime laterDate, int threshold )
	{
		return Math.abs( DateHelper.milliDiff( earlierDate, laterDate ) ) <= threshold;
	}

	// Format/parse functions
	public static String toYYYYMMDDHHMMSS( DateTime date )
	{
		return yyyymmddhhmmssFormatter.print( date );
	}

	public static DateTime validDttmFromYYYYMMDDHHMMSS( String dateString )
	{
		LocalDateTime parseLocalDateTime = yyyymmddhhmmssFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return fromYYYYMMDDHHMMSS( dateString );
	}

	public static DateTime fromYYYYMMDDHHMMSS( String dateString )
	{
		return yyyymmddhhmmssFormatter.parseDateTime( dateString );
	}

	public static String toYYYYMMDDHHMMSSContinuous( DateTime date )
	{
		return yyyymmddhhmmssContinuousFormatter.print( date );
	}

	public static DateTime validDttmFromYYYYMMDDHHMMSSContinuous( String dateString )
	{
		LocalDateTime parseLocalDateTime = yyyymmddhhmmssContinuousFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return fromYYYYMMDDHHMMSSContinuous( dateString );
	}

	public static DateTime fromYYYYMMDDHHMMSSContinuous( String dateString )
	{
		return yyyymmddhhmmssContinuousFormatter.parseDateTime( dateString );
	}

	public static String toYYYYMMDD( DateTime date )
	{
		return yyyymmddFormatter.print( date );
	}

	public static DateTime fromYYYYMMDD( String dateString )
	{
		LocalDateTime parseLocalDateTime = yyyymmddFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return yyyymmddFormatter.parseDateTime( dateString );
	}

	public static String toMMDDYYYYWithSlash( DateTime date )
	{
		return mmddyyyyWithSlashFormatter.print( date );
	}

	public static DateTime fromMMDDYYYYWithSlash( String dateString )
	{
		return mmddyyyyWithSlashFormatter.parseDateTime( dateString );
	}

	public static String toHHMMSS( DateTime date )
	{
		return hhmmssFormatter.print( date );
	}

	public static DateTime fromHHMMSS( String dateString )
	{
		return hhmmssFormatter.parseDateTime( dateString );
	}

	public static boolean isBeforeOrEqual( DateTime date, DateTime compareTo )
	{
		return ( date.isBefore( compareTo ) || date.isEqual( compareTo ) );
	}

	public static boolean isAfterOrEqual( DateTime date, DateTime compareTo )
	{
		return ( date.isAfter( compareTo ) || date.isEqual( compareTo ) );
	}

	public static boolean rangesOverlap( DateTime from1, DateTime to1, DateTime from2, DateTime to2 )
	{
		return ( ( DateHelper.isBeforeOrEqual( from1, to2 ) ) && ( DateHelper.isAfterOrEqual( to1, from2 ) ) );
	}

	public static int getDaysInMonth( DateTime datetime )
	{
		return getDaysInMonths( datetime, 1 );
	}

	public static int getDaysInMonths( DateTime datetime, int monthDiff )
	{
		DateTime startDttm = getValidDttmAfter( datetime.getYearOfEra(), datetime.getMonthOfYear(), 1, 0, 0, 0, 0 );
		DateTime endDttm = DateHelper.addMonths( startDttm, monthDiff );

		return dayDiff( startDttm, endDttm );
	}

	public static long getSecondsLeftInMonth( DateTime dateTime )
	{
		DateTime beginNextMonth = getValidDttmAfter( dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0, 0 ).plus( Period.months( 1 ) );

		return secondDiff( dateTime, beginNextMonth );
	}

	public static long getSecondsInMonth( DateTime dateTime )
	{
		DateTime beginThisMonth = getValidDttmAfter( dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0, 0 );
		DateTime beginNextMonth = beginThisMonth.plus( Period.months( 1 ) );

		return secondDiff( beginThisMonth, beginNextMonth );
	}

	public static DateTime earliest( DateTime dateTime1, DateTime dateTime2 )
	{
		if ( dateTime1.isAfter( dateTime2 ) )
			return dateTime2;

		return dateTime1;
	}

	public static DateTime latest( DateTime dateTime1, DateTime dateTime2 )
	{
		if ( dateTime1.isBefore( dateTime2 ) )
			return dateTime2;

		return dateTime1;
	}

	public static int compareTo( DateTime firstObject, DateTime secondObject )
	{
		return firstObject.equals( secondObject ) ? 0 : firstObject.isAfter( secondObject.getMillis() ) ? 1 : -1;
	}

	// Get a DateTime that is the instant before the specified DateTime.
	// This can be used to easily get the "to" date which ends right before
	// the next "from" date for day based times e.g. TariffPeriods
	public static DateTime getInstantBefore( DateTime date )
	{
		if ( date == null )
			return null;

		return DateHelper.addSeconds( date, -1 );
	}

	/// Get a DateTime that is the instant after the specified DateTime.
	/// This can be used to easily get the next "from" date after a "to" date ends.
	public static DateTime getInstantAfter( DateTime date )
	{
		if ( date == null )
			return null;

		return DateHelper.addSeconds( date, 1 );
	}

	// Builds the query to return all rows with from/to dates that overlap the specified date.
	public static String HQLOverlapsDateTime( String dateTimeFromColumn, String dateTimeToColumn, String paramName )
	{
		// builds the query to return all rows that overlap the specified date
		return dateTimeFromColumn + " <= :" + paramName + " and " + dateTimeToColumn + " >= :" + paramName;
	}

	public static DateTime copyTimeInto( DateTime fromDttm, DateTime timeDttm )
	{
		LocalDateTime localDateTime = new LocalDateTime( fromDttm.getYear(), fromDttm.getMonthOfYear(), fromDttm.getDayOfMonth(), timeDttm.getHourOfDay(), timeDttm.getMinuteOfHour(), timeDttm.getSecondOfMinute(), timeDttm.getMillisOfSecond() );
		if ( isLocalTimeGap( localDateTime ) )
			return getValidTime( localDateTime );
		else
			return new DateTime( fromDttm.getYear(), fromDttm.getMonthOfYear(), fromDttm.getDayOfMonth(), timeDttm.getHourOfDay(), timeDttm.getMinuteOfHour(), timeDttm.getSecondOfMinute(), timeDttm.getMillisOfSecond() );
	}

	public static DateTime roundToHour( DateTime dateTime )
	{
		return dateTime.hourOfDay().roundFloorCopy();
	}

	public static DateTime justDate( DateTime dateTime )
	{
		LocalDateTime localDateTime = new LocalDateTime( dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0, 0, 0 );
		if ( isLocalTimeGap( localDateTime ) )
			return getValidTime( localDateTime );
		else
			return new DateTime( dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0, 0, 0 );
	}

	public static DateTime[] getDayRange( DateTime dateTime )
	{
		DateTime range[] = new DateTime[2];
		range[0] = getFirstMinuteOfDay( dateTime );
		range[1] = getLastMinuteOfDay( dateTime );
		return range;
	}

	public static DateTime[] getMonthRange( DateTime dateTime )
	{
		DateTime range[] = new DateTime[2];
		range[0] = getValidDttmAfter( dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0, 0 );
		range[1] = range[0].plusMonths( 1 ).minusMillis( 1 );
		return range;
	}

	public static DateTime[] getYearRange( DateTime dateTime )
	{
		DateTime range[] = new DateTime[2];
		range[0] = getValidDttmAfter( dateTime.getYear(), 1, 1, 0, 0, 0, 0 );
		range[1] = range[0].plusYears( 1 ).minusMillis( 1 );
		return range;
	}

	public static String formatPeriodToReadable( DateTime startTime, DateTime endTime )
	{
		Period period = new Period( startTime, endTime );

		StringBuffer sb = new StringBuffer();
		boolean restOfPeriod = false;

		if ( period.getDays() > 0 )
		{
			restOfPeriod = true;
			sb.append( period.getDays() );

			if ( period.getDays() > 1 )
			{
				sb.append( " days " );
			}
			else
			{
				sb.append( " day " );
			}
		}

		if ( restOfPeriod || period.getHours() > 0 )
		{
			restOfPeriod = true;
			sb.append( period.getHours() );

			if ( period.getHours() > 1 )
			{
				sb.append( " hours " );
			}
			else
			{
				sb.append( " hour " );
			}
		}

		if ( restOfPeriod || period.getMinutes() > 0 )
		{
			restOfPeriod = true;
			sb.append( period.getMinutes() );

			if ( period.getMinutes() > 1 )
			{
				sb.append( " minutes " );
			}
			else
			{
				sb.append( " minute " );
			}
		}

		if ( restOfPeriod || period.getSeconds() > 0 )
		{
			restOfPeriod = true;
			sb.append( period.getSeconds() );

			if ( period.getSeconds() > 1 )
			{
				sb.append( " seconds" );
			}
			else
			{
				sb.append( " second" );
			}
		}

		// if < 0 seconds write milli seconds
		if ( !restOfPeriod )
		{
			sb.append( period.getMillis() );

			if ( period.getMillis() > 1 )
			{
				sb.append( " milliseconds" );
			}
			else
			{
				sb.append( " millisecond" );
			}
		}

		return sb.toString();
	}

	/**
	 *
	 * @param dateTime
	 * @param format
	 * @returns valid date-time after given date-time
	 */
	public static DateTime getValidDateTime( String dateTime, DateTimeFormatter formatter )
	{
		if ( dateTime == null || formatter == null )
			return null;

		LocalDateTime parseLocalDateTime = formatter.parseLocalDateTime( dateTime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return formatter.parseDateTime( dateTime );
	}

	/**
	 *
	 * @param dateTime
	 * @param format
	 * @returns valid date-time after given date-time
	 */
	public static DateTime getValidDateTime( String dateTime, String format )
	{
		if ( dateTime == null || format == null )
			return null;

		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );
		LocalDateTime parseLocalDateTime = formatter.parseLocalDateTime( dateTime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return formatter.parseDateTime( dateTime );
	}

	public static DateTime getDateTime( String dateTime, String format )
	{
		if ( dateTime == null || format == null )
			return null;

		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );

		return formatter.parseDateTime( dateTime );
	}

	// Formats a datetime into a string representation for storage in a file or
	// generic string column. Ensures we know what format it has been stored
	// as regardless of the users regional settings.
	public static String formatForStorage( DateTime dateTime )
	{
		// first check for nulls - we leave MAX_DATETIME dates as is for storage
		if ( dateTime == null || dateTime.equals( NCashConstant.MAX_DATE_TIME ) )
		{
			return "";
		}

		// format to a standard known format so we can parse regardless of
		// regional settings etc.
		/*if ( EnvironmentProperties.getServerProperties().getSvrProcessMilliSec() )
			return dateStringForStorageMilliFormatter.print( dateTime );*/
		else
			return dateStringForStorageFormatter.print( dateTime );
	}

	public static String formatToMMDDYYYY( DateTime dateTime )
	{
		// first check for nulls - we leave MAX_DATETIME dates as is for storage
		if ( dateTime == null || dateTime.equals( NCashConstant.MAX_DATE_TIME ) )
		{
			return "";
		}

		// format to a standard known format so we can parse regardless of
		// regional settings etc.
		return mmddyyyyhhmmssFormatter.print( dateTime );
	}

	//In case the user prefered calendar is  arbic convert the date to islamic date and
	// display the date in arbic format 
	//	public static String formatForDispalyInStorageFormat( DateTime dateTime )
	//	{
	//
	//		return  formatForDispalyInStorageFormat(dateTime, UserSession.instance().getUserProperties());
	//	}

	//	public static String formatForDispalyInStorageFormat( DateTime dateTime, UserProperties userProperties )
	//	{
	//
	//		if ( "hi-SA".equals( userProperties.getUsrCalendarLocale()))
	//		{
	//
	//			// first check for nulls - we leave MAX_DATETIME dates as is for storage
	//			if ( dateTime == null || dateTime.equals( SparkConstant.MAX_DATE_TIME ) )
	//			{
	//				return "";
	//			}
	//
	//			// format to a standard known format so we can parse regardless of
	//			// regional settings etc.
	//
	//			return dateStringForStorageFormatter.print( dateTime.withChronology( IslamicChronology.getInstance() ));
	//
	//		}
	//
	//		return formatForStorage( dateTime );
	//	}

	//	public static String getDateByCalendar(DateTime dateTime, DateTimeFormatter defaultDateFormatter)
	//	{
	//		if(dateTime==null || defaultDateFormatter ==null ) return "";
	//		
	//		// If the user selected calendar is Arabic then change the date to arabic
	//		 if(UserSession.instance().getUserProperties().getUsrCalendarLocale().equals( "hi-SA" ))
	//		 	return  defaultDateFormatter.print( dateTime.withChronology( IslamicChronology.getInstance() )) ;
	//
	//		return	defaultDateFormatter.print( dateTime );
	//	}
	// Parses a string which has been formated using the FormatDateTimeForStorage
	// method. Ensures we can retrieve the date information regardless of
	// what the users regional settings were when the date was saved.
	public static DateTime parseValidDttmFromStorage( String dateTime )
	{
		// first check for nulls
		if ( dateTime == null || dateTime.length() == 0 )
			return null;

		// format from our standard known format so we can parse regardless of
		// regional settings etc.
		LocalDateTime parseLocalDateTime;
		parseLocalDateTime = dateStringForStorageFormatter.parseLocalDateTime( dateTime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );

		else
			return dateStringForStorageFormatter.parseDateTime( dateTime );
	}

	public static DateTime parseValidDttmFromStorage( String dateTime, Boolean milliSec )
	{
		// first check for nulls
		if ( dateTime == null || dateTime.length() == 0 )
			return null;

		// format from our standard known format so we can parse regardless of
		// regional settings etc.
		LocalDateTime parseLocalDateTime;
		if ( milliSec )
			parseLocalDateTime = dateStringForStorageMilliFormatter.parseLocalDateTime( dateTime );
		else
			parseLocalDateTime = dateStringForStorageFormatter.parseLocalDateTime( dateTime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else if ( milliSec )
			return dateStringForStorageMilliFormatter.parseDateTime( dateTime );
		else
			return dateStringForStorageFormatter.parseDateTime( dateTime );
	}

	public static DateTime parseFromStorage( String dateTime )
	{
		if ( dateTime == null || dateTime.length() == 0 )
			return null;
		return dateStringForStorageFormatter.parseDateTime( dateTime );
	}

	public static DateTime applySlidingWindow( DateTime dateTime, DateTime pivotDttm )
	{
		int year = dateTime.getYear();

		// no need if the year has 4 digits
		if ( year >= 1000 )
			return dateTime;

		// noramlise the decade first
		if ( year < 10 )
		{
			int pivotDecade = pivotDttm.getYear() % 100;

			year = normalizeYear( year, pivotDecade, 10 );
		}

		if ( year < 100 )
		{
			int pivotCentury = pivotDttm.getYear() % 1000;

			year = normalizeYear( year, pivotCentury, 100 );
		}

		if ( year < 1000 )
		{
			int pivotYear = pivotDttm.getYear();

			year = normalizeYear( year, pivotYear, 1000 );
		}

		// move the year of the date time if necessary
		return dateTime.plus( Period.years( year - dateTime.getYear() ) );
	}

	private static int normalizeYear( int year, int pivotYear, int divider )
	{
		int overflow = divider * 10;
		int outOfBounds = divider / 2;

		// add the pivot decade only to the year
		year = ( pivotYear / divider ) * divider + year;

		if ( year > pivotYear )
		{
			// when exactly the out of bounds always choose the lower year
			if ( year - pivotYear >= outOfBounds )
			{
				year = year - divider;

				if ( year < 0 )
					year = year + overflow;
			}
		}
		else if ( year < pivotYear )
		{
			if ( pivotYear - year > outOfBounds )
			{
				year = year + divider;

				if ( year > overflow )
					year = year - overflow;
			}
		}

		return year;
	}

	public static Date convertStringToDate( String inputDateFmt, String outputDateFmt, String date )
	{
		SimpleDateFormat sqlServerDateFormat = new SimpleDateFormat( inputDateFmt );
		Date dateObj = null;

		try
		{
			DateTime jodaDateTime = null;
			jodaDateTime = new DateTime( sqlServerDateFormat.parse( date ) );

			DateTimeFormatter fmt = DateTimeFormat.forPattern( outputDateFmt );
			fmt.print( jodaDateTime );
			dateObj = jodaDateTime.toDate();
		}
		catch ( ParseException e )
		{
			// returns null if parsing fails, caller should ensure that right format is being passed
		}

		return dateObj;
	}

	public static String convertJDKDateToString( DateTimeFormatter format, Date date )
	{
		return new DateTime( date ).toString( format );
	}

	public static Date convertStringToJDKDate( DateTimeFormatter format, String date )
	{
		return format.parseDateTime( date ).toDate();
	}

	public static DateTime fromDDMMYYYY( String dateString )
	{
		return ddMMyyyyFormatter.parseDateTime( dateString );
	}

	public static DateTime getValidDttmFromMMDDYYYYHHMMSS( String dateString )
	{
		LocalDateTime parseLocalDateTime = mmddyyyyhhmmssFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return mmddyyyyhhmmssFormatter.parseDateTime( dateString );
	}

	public static DateTime fromMMDDYYYYHHMMSS( String dateString )
	{
		return mmddyyyyhhmmssFormatter.parseDateTime( dateString );
	}

	public static boolean isEmptyDate( DateTime dateTime )
	{
		// check for the NULL_DATETIME constant
		if ( dateTime.equals( nullDate ) )
		{
			return true;
		}

		// check for either the MIN_DATETIME or MAX_DATETIME constants
		if ( dateTime.equals( minDate ) || dateTime.equals( maxDate ) )
		{
			return true;
		}

		// if we get here the date is not empty
		return false;
	}

	public static String convertToTimeSpan( Object value )
	{
		long val = 0;
		try
		{
			val = Long.parseLong( value.toString() );
		}
		catch ( Exception e )
		{
			return "";
		}

		int hours = 0;
		int minutes = 0;
		long seconds = val;

		while ( seconds >= 60 )
		{
			minutes++;
			if ( minutes >= 60 )
			{
				hours++;
			}
			seconds = seconds - 60;
		}
		return formatTime( hours ) + ":" + formatTime( minutes ) + ":" + formatTime( seconds );
	}

	private static String formatTime( long value )
	{
		return ( value / 10 > 0 ) ? new Long( value ).toString() : "0" + value;
	}

	public static String convertJodaFormatToExtDateFormat( String pattern )
	{
		// copied from Ext docs
		//		Format  Description                                                               Example returned values
		//		------  -----------------------------------------------------------------------   -----------------------
		//		  d     Day of the month, 2 digits with leading zeros                             01 to 31
		//		  D     A short textual representation of the day of the week                     Mon to Sun
		//		  j     Day of the month without leading zeros                                    1 to 31
		//		  l     A full textual representation of the day of the week                      Sunday to Saturday
		//		  N     ISO-8601 numeric representation of the day of the week                    1 (for Monday) through 7 (for Sunday)
		//		  S     English ordinal suffix for the day of the month, 2 characters             st, nd, rd or th. Works well with j
		//		  w     Numeric representation of the day of the week                             0 (for Sunday) to 6 (for Saturday)
		//		  z     The day of the year (starting from 0)                                     0 to 364 (365 in leap years)
		//		  W     ISO-8601 week number of year, weeks starting on Monday                    01 to 53
		//		  F     A full textual representation of a month, such as January or March        January to December
		//		  m     Numeric representation of a month, with leading zeros                     01 to 12
		//		  M     A short textual representation of a month                                 Jan to Dec
		//		  n     Numeric representation of a month, without leading zeros                  1 to 12
		//		  t     Number of days in the given month                                         28 to 31
		//		  L     Whether it's a leap year                                                  1 if it is a leap year, 0 otherwise.
		//		  o     ISO-8601 year number (identical to (Y), but if the ISO week number (W)    Examples: 1998 or 2004
		//				belongs to the previous or next year, that year is used instead)
		//		  Y     A full numeric representation of a year, 4 digits                         Examples: 1999 or 2003
		//		  y     A two digit representation of a year                                      Examples: 99 or 03
		//		  a     Lowercase Ante meridiem and Post meridiem                                 am or pm
		//		  A     Uppercase Ante meridiem and Post meridiem                                 AM or PM
		//		  g     12-hour format of an hour without leading zeros                           1 to 12
		//		  G     24-hour format of an hour without leading zeros                           0 to 23
		//		  h     12-hour format of an hour with leading zeros                              01 to 12
		//		  H     24-hour format of an hour with leading zeros                              00 to 23
		//		  i     Minutes, with leading zeros                                               00 to 59
		//		  s     Seconds, with leading zeros                                               00 to 59
		//		  u     Milliseconds, with leading zeros                                          001 to 999
		//		  O     Difference to Greenwich time (GMT) in hours and minutes                   Example: +1030
		//		  P     Difference to Greenwich time (GMT) with colon between hours and minutes   Example: -08:00
		//		  T     Timezone abbreviation of the machine running the code                     Examples: EST, MDT, PDT ...
		//		  Z     Timezone offset in seconds (negative if west of UTC, positive if east)    -43200 to 50400
		//		  c     ISO 8601 date                                                             2007-04-17T15:19:21+08:00
		//		  U     Seconds since the Unix Epoch (January 1 1970 00:00:00 GMT)                1193432466 or -2138434463

		// TODO: needs more work doing
		pattern = StringHelper.replace( pattern, "dd", "d" );
		pattern = StringHelper.replace( pattern, "d", "j" );

		pattern = StringHelper.replace( pattern, "MMMM", "F" );
		pattern = StringHelper.replace( pattern, "MMM", "M" );
		pattern = StringHelper.replace( pattern, "MM", "m" );
		pattern = StringHelper.replace( pattern, "M", "n" );

		pattern = StringHelper.replace( pattern, "yyyy", "Y" );
		pattern = StringHelper.replace( pattern, "yy", "y" );

		pattern = StringHelper.replace( pattern, "hh", "h" );
		pattern = StringHelper.replace( pattern, "HH", "H" );
		pattern = StringHelper.replace( pattern, "mm", "i" );
		pattern = StringHelper.replace( pattern, "ss", "s" );

		pattern = StringHelper.replace( pattern, "a", "A" );

		return pattern;
	}

	public static String convertOracleFormatToJodaFormat( String format )
	{

		//Not Supported
		if ( format.contains( "DAY" ) || format.contains( "DL" ) || format.contains( "DS" ) || format.contains( "DY" ) || format.contains( "EE" ) || format.contains( "E" ) || format.contains( "FM" ) || format.contains( "FX" ) || format.contains( "IYYY" ) || format.contains( "IYY" ) || format.contains( "IY" ) || format.contains( "J" ) || format.contains( "Q" ) || format.contains( "RM" ) || format.contains( "RR" ) || format.contains( "RRRR" ) || format.contains( "SSSSS" ) || format.contains( "TS" ) || format.contains( "TZD" ) || format.contains( "TZH" ) || format.contains( "TZM" ) || format.contains( "TZR" ) || format.contains( "WW" ) || format.contains( "W" ) || format.contains( "X" ) || format.contains( "Y,YYY" ) || format.contains( "YEAR" ) || format.contains( "SYEAR" ) || format.contains( "SYYYY" ) )
		{
			throw new IllegalArgumentException( "Unsupported joda format for oracle format " + format );
		}

		format = format.toUpperCase();
		format = StringHelper.replace( format, "AD", "G" );
		format = StringHelper.replace( format, "A.D.", "G" );
		format = StringHelper.replace( format, "BC", "G" );
		format = StringHelper.replace( format, "B.C.", "G" );

		format = StringHelper.replace( format, "AM", "a" );
		format = StringHelper.replace( format, "A.M.", "a" );
		format = StringHelper.replace( format, "PM", "a" );
		format = StringHelper.replace( format, "P.M.", "a" );

		format = StringHelper.replace( format, "CC", "C" );
		format = StringHelper.replace( format, "SCC", "C" );

		format = StringHelper.replace( format, "DAY", "E" );

		boolean ignoreDayofWeekConversion = format.contains( "DDD" );
		format = StringHelper.replace( format, "DDD", "D" );
		format = StringHelper.replace( format, "DD", "d" );

		if ( !ignoreDayofWeekConversion )
			format = StringHelper.replace( format, "D", "e" );

		format = StringHelper.replace( format, "HH24", "H" );
		format = StringHelper.replace( format, "HH12", "h" );
		format = StringHelper.replace( format, "HH", "h" );

		format = StringHelper.replace( format, "IW", "w" );

		boolean isMonthLongForm = format.contains( "MONTH" ) || format.contains( "MON" );

		format = StringHelper.replace( format, "MONTH", "MMM" );
		format = StringHelper.replace( format, "MON", "MMM" );

		if ( !isMonthLongForm )
			format = StringHelper.replace( format, "MM", "M" );

		format = StringHelper.replace( format, "MI", "m" );
		format = StringHelper.replace( format, "SS", "s" );

		format = StringHelper.replace( format, "TZR", "z" );

		format = StringHelper.replace( format, "YYYY", "y" );
		format = StringHelper.replace( format, "YYY", "y" );
		format = StringHelper.replace( format, "YY", "y" );
		format = StringHelper.replace( format, "Y", "y" );

		return format;
	}

	/**
	 * Replacement for joda's DateTime(int year,int monthOfYear,int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond ) function
	 * to avoid illegal instants
	 * @param dateTime
	 * @param hourOfDay
	 * @param minuteOfHour
	 * @param secondOfMinute
	 * @param millisOfSecond
	 * @return
	 */
	public static DateTime getValidDttmAfter( int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond )
	{
		LocalDateTime parseLocalDateTime = new LocalDateTime( year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return new DateTime( year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond );
	}

	/**
	 * Replacement for joda's DateTime().withTime( int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond ) function
	 * to avoid illegal instants
	 * @param dateTime
	 * @param hourOfDay
	 * @param minuteOfHour
	 * @param secondOfMinute
	 * @param millisOfSecond
	 * @return
	 */
	public static DateTime getValidDttmWithTime( DateTime dateTime, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond )
	{
		LocalDateTime localDateTime = new LocalDateTime( dateTime.getMillis() ).withTime( hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond );
		if ( isLocalTimeGap( localDateTime ) )
			return getValidTime( localDateTime );
		else
			return dateTime.withTime( hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond );
	}

	/**
	 * Gets the first available valid minute of day considering DST
	 * @param day
	 * @return
	 */
	public static DateTime getFirstMinuteOfDay( DateTime day )
	{
		return day.withTimeAtStartOfDay();
	}

	/**
	 * Gets the last available valid minute of day considering DST
	 * @param day
	 * @return
	 */
	public static DateTime getLastMinuteOfDay( DateTime day )
	{
		return day.toLocalDate().plusDays( 1 ).toDateTimeAtStartOfDay( day.getZone() ).minusMillis( 1 );
	}

	/**
	 * Gets the last available valid minute of day considering DST
	 * @param day
	 * @return
	 */
	public static DateTime getLastMinuteOfDayWithoutMillis( DateTime day )
	{
		return day.toLocalDate().plusDays( 1 ).toDateTimeAtStartOfDay( day.getZone() ).minusSeconds( 1 );
	}

	public static boolean isLocalTimeGap( LocalDateTime localDateTime )
	{
		DateTimeZone zoneToBeSet = DateTimeZone.getDefault();
		return zoneToBeSet.isLocalDateTimeGap( localDateTime );
	}

	/**
	 * considering DST, returns localDateTime itself if it is valid for current joda time zone
	 * else returns next available valid time
	 * @param localDateTime
	 * @return
	 */
	private static DateTime getValidTime( LocalDateTime localDateTime )
	{
		DateTimeZone zoneToBeSet = DateTimeZone.getDefault();
		if ( zoneToBeSet.isLocalDateTimeGap( localDateTime ) )
		{
			if ( zoneToBeSet.getStandardOffset( 0 ) < 0 )
				return new DateTime( zoneToBeSet.nextTransition( localDateTime.toDateTime( DateTimeZone.UTC ).getMillis() ) );
			else
				return new DateTime( zoneToBeSet.previousTransition( localDateTime.toDateTime( DateTimeZone.UTC ).getMillis() ) ).plusMillis( 1 );
		}
		else
			return localDateTime.toDateTime();
	}

}
