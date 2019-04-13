package com.asjngroup.ncash.common;

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

import com.asjngroup.ncash.common.util.StringHelper;

public class NCashDateFormatter
{
	static
	{
		// stash the actual system joda timezone
		systemTimezone = DateTimeZone.getDefault();
	}

	private static final DateTimeZone systemTimezone;

	public static final String dateFormatStringForStorage = "yyyy-MM-dd HH:mm:ss";
	public static final String dateFormatStringForOracleStorage = "yyyy-MM-dd HH24:mi:ss";
	public static final String dateFormatStringForMySQlStorage = "yyyy-MM-dd HH24:mi:ss";

	public static DateTime minDate = new DateTime( 1900, 1, 1, 1, 1, 1, 0, systemTimezone );
	private static DateTime maxDate = new DateTime( 9999, 1, 1, 23, 59, 59, 0, systemTimezone );

	public static DateTime getMinimumDate()
	{
		return minDate;
	}

	public static DateTime getMaximumDate()
	{
		return maxDate;
	}

	public static final int SECONDS_IN_DAY = 60 * 24;
	public static final int SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;

	public static final DateTimeFormatter mmddyhhmmssaFormatter = DateTimeFormat.forPattern( "MM/dd/y hh:mm:ss a" );
	public static final DateTimeFormatter dateStringForStorageFormatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" );
	public static final DateTimeFormatter dateStringForMilliSecStorageFormatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss.SSSSSS" );
	public static final DateTimeFormatter yyyymmddhhmmssFormatter = DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss" );
	public static final DateTimeFormatter yyyymmddhhmmssSSSSSSFormatter = DateTimeFormat.forPattern( "yyyyMMdd HH:mm:ss.SSSSSS" );
	public static final DateTimeFormatter yyyymmddhhmmssContinuousFormatter = DateTimeFormat.forPattern( "yyyyMMddHHmmss" );
	public static final DateTimeFormatter yyyymmddFormatter = DateTimeFormat.forPattern( "yyyyMMdd" );
	public static final DateTimeFormatter hhmmssFormatter = DateTimeFormat.forPattern( "HH:mm:ss" );
	public static final DateTimeFormatter mmddyyyyWithSlashFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy" );

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
		DateTimeUtils.setCurrentMillisProvider( new TimeZoneOffsetProvider( systemTimezone ) );

		// set default timezones for joda and java
		setGlobalJodaTimeZone( DateTimeZone.UTC );
		setGlobalJavaTimeZone( DateTimeZone.UTC.toTimeZone() );
	}

	public static void setGlobalJodaTimeZone( DateTimeZone dateTimeZone )
	{
		// default the system joda timezone
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
		return Math.abs( NCashDateFormatter.secondDiff( earlierDate, laterDate ) ) <= threshold;
	}

	public static boolean isWithinTimeThresholdMilli( DateTime earlierDate, DateTime laterDate, int threshold )
	{
		return Math.abs( NCashDateFormatter.milliDiff( earlierDate, laterDate ) ) <= threshold;
	}

	// Format/parse functions
	public static String toYYYYMMDDHHMMSS( DateTime date )
	{
		return yyyymmddhhmmssFormatter.print( date );
	}

	public static DateTime fromYYYYMMDDHHMMSS( String dateString )
	{
		return yyyymmddhhmmssFormatter.parseDateTime( dateString );
	}

	public static DateTime validDttmFromYYYYMMDDHHMMSS( String dateString )
	{
		LocalDateTime parseLocalDateTime = yyyymmddhhmmssFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return fromYYYYMMDDHHMMSS( dateString );
	}

	public static String toYYYYMMDDHHMMSSContinuous( DateTime date )
	{
		return yyyymmddhhmmssContinuousFormatter.print( date );
	}

	public static DateTime fromYYYYMMDDHHMMSSContinuous( String dateString )
	{
		return yyyymmddhhmmssContinuousFormatter.parseDateTime( dateString );
	}

	public static DateTime validDttmFromYYYYMMDDHHMMSSContinuous( String dateString )
	{
		LocalDateTime parseLocalDateTime = yyyymmddhhmmssContinuousFormatter.parseLocalDateTime( dateString );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return fromYYYYMMDDHHMMSSContinuous( dateString );
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
		return ( ( NCashDateFormatter.isBeforeOrEqual( from1, to2 ) ) && ( NCashDateFormatter.isAfterOrEqual( to1, from2 ) ) );
	}

	public static int getDaysInMonth( DateTime datetime )
	{
		return getDaysInMonths( datetime, 1 );
	}

	public static int getDaysInMonths( DateTime datetime, int monthDiff )
	{
		DateTime startDttm;
		LocalDateTime localDateTime = new LocalDateTime( datetime.getYearOfEra(), datetime.getMonthOfYear(), 1, 0, 0, 0, 0 );
		if ( isLocalTimeGap( localDateTime ) )
			startDttm = getValidTime( localDateTime );
		else
			startDttm = new DateTime( datetime.getYearOfEra(), datetime.getMonthOfYear(), 1, 0, 0, 0, 0 );

		DateTime endDttm = NCashDateFormatter.addMonths( startDttm, monthDiff );

		return dayDiff( startDttm, endDttm );
	}

	public static long getSecondsLeftInMonth( DateTime dateTime )
	{
		DateTime beginNextMonth = NCashDateFormatter.getValidDttmAfter( dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0, 0 ).plus( Period.months( 1 ) );

		return secondDiff( dateTime, beginNextMonth );
	}

	public static long getSecondsInMonth( DateTime dateTime )
	{
		DateTime beginThisMonth = NCashDateFormatter.getValidDttmAfter( dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0, 0 );
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

		return NCashDateFormatter.addSeconds( date, -1 );
	}

	/// Get a DateTime that is the instant after the specified DateTime.
	/// This can be used to easily get the next "from" date after a "to" date ends.
	public static DateTime getInstantAfter( DateTime date )
	{
		if ( date == null )
			return null;

		return NCashDateFormatter.addSeconds( date, 1 );
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
	public static DateTime formatValidDate( String dateTime, String format )
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

	public static DateTime formatDate( String dateTime, String format )
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
		if ( dateTime == null )
			return "";

		// format to a standard known format so we can parse regardless of
		// regional settings etc.
		return dateStringForStorageFormatter.print( dateTime );
	}

	public static DateTime parseValidDateTimeFromStorage( String dateTime )
	{
		if ( dateTime.length() == 0 )
			return null;

		LocalDateTime parseLocalDateTime = dateStringForStorageFormatter.parseLocalDateTime( dateTime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return dateStringForStorageFormatter.parseDateTime( dateTime );
	}

	// Parses a string which has been formated using the FormatDateTimeForStorage
	// method. Ensures we can retrieve the date information regardless of
	// what the users regional settings were when the date was saved.
	public static DateTime parseFromStorage( String dateTime )
	{
		// first check for nulls
		if ( dateTime == null || dateTime.length() == 0 )
			return null;

		// format from our standard known format so we can parse regardless of
		// regional settings etc.
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

	public static DateTime convertStringToValidDatetime( String datetime )
	{
		LocalDateTime parseLocalDateTime = mmddyhhmmssaFormatter.parseLocalDateTime( datetime );
		if ( isLocalTimeGap( parseLocalDateTime ) )
			return getValidTime( parseLocalDateTime );
		else
			return mmddyhhmmssaFormatter.parseDateTime( datetime );
	}

	public static DateTime convertStringToJDKDatetime( String datetime )
	{
		return mmddyhhmmssaFormatter.parseDateTime( datetime );
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

	public static boolean isLocalTimeGap( DateTime dateTime )
	{
		DateTimeZone zoneToBeSet = DateTimeZone.getDefault();
		LocalDateTime localDateTime = dateTime.toLocalDateTime();
		return zoneToBeSet.isLocalDateTimeGap( localDateTime );
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

	public static DateTime getDateTimeWithoutMilli( DateTime dateTime )
	{
		DateTime dt = new DateTime( dateTime.getMillis() - dateTime.getMillisOfSecond() );
		return dt;
	}

	public static DateTime fromYYYYMMDDHHMMSSSSSS( String dateStr )
	{

		return yyyymmddhhmmssSSSSSSFormatter.parseDateTime( dateStr );
	}
}
