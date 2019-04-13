package com.asjngroup.ncash.common.database.helper;


import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import java.util.HashMap;
import java.util.Map;

import com.asjngroup.ncash.common.database.hibernate.HibernateObject;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;


public class SequenceNumberHelper
{
	private LongIdHelper longSequenceNumberHelper;
	private static Map<Integer, Map<String, SequenceNumberHelper>> sequenceNumberHelpers = new HashMap();

	private static final Map<String, Long> sequenceCache = new HashMap();
	private static final int STARTING_ID = -1073741824;

	public SequenceNumberHelper( Class< ? > clazz, int blockSize )
	{
		this( clazz.getSimpleName(), blockSize );
	}

	public SequenceNumberHelper( String objectKey, int blockSize )
	{
		if ( blockSize <= 0 )
		{
			throw new IllegalArgumentException( "Invalid block size " + blockSize + " passed to id helper" );
		}
		this.longSequenceNumberHelper = new LongIdHelper( objectKey, blockSize );
	}

	public long getNextId() throws HibernateException
	{
		long nextLong = this.longSequenceNumberHelper.getNextId();

		if ( nextLong >= 2147483647L )
		{
			throw new NCashRuntimeException( "id helper value has gone out of range. Current value is %1 max value is %2", new Object[]
			{ Long.valueOf( nextLong ), Integer.valueOf( 2147483647 ) } );
		}
		return ( long ) nextLong;
	}

	public static synchronized IdHelper getIdHelper( Class< ? > clazz, int blockSize )
	{
		return getIdHelper( null, clazz, blockSize );
	}

	public static synchronized IdHelper getIdHelper( Integer stsId, Class< ? > clazz, int blockSize )
	{
		String objectKey = clazz.getSimpleName();
		return getIdHelper( stsId, objectKey, blockSize );
	}

	public static synchronized IdHelper getIdHelper( String objectKey, int blockSize )
	{
		return getIdHelper( null, objectKey, blockSize );
	}

	public static synchronized IdHelper getIdHelper( Integer stsId, String objectKey, int blockSize )
	{
		Integer stsIdKey = ( stsId == null ) ? Integer.valueOf( -1 ) : stsId;
		Map map = ( Map ) sequenceNumberHelpers.get( stsIdKey );
		if ( map == null )
		{
			map = new HashMap();
			sequenceNumberHelpers.put( stsIdKey, map );
		}

		IdHelper idHelper = ( IdHelper ) map.get( objectKey );
		if ( idHelper == null )
		{
			idHelper = new IdHelper( objectKey, blockSize );
			map.put( objectKey, idHelper );
		}
		return idHelper;
	}

	public static <T extends HibernateObject> T allocateId( T object )
	{
		if ( object == null )
		{
			throw new NullPointerException();
		}
		Class clazz = Hibernate.getClass( object );

		int id = new IdHelper( clazz, 1 ).getNextId();

		object.setId( id );

		return object;
	}

	public static long generateId( Class< ? > clazz )
	{
		return generateId( clazz, 1 );
	}

	public static long generateId( Class< ? > clazz, int required )
	{
		return new IdHelper( HibernateUtil.unproxyEntityClass( clazz ).getSimpleName(), required ).getNextId();
	}

	public static long generateId( String objectKey )
	{
		return generateId( objectKey, 1 );
	}

	public static int generateId( String objectKey, int required )
	{
		return new IdHelper( objectKey, required ).getNextId();
	}

	public static long getIdFor( Class< ? > clazz )
	{
		String name = clazz.getName();
		return getIdFor( name );
	}

	public static long getIdFor( String name )
	{
		Long result = ( Long ) sequenceCache.get( name );
		if ( ( result == null ) || ( result.intValue() >= 0 ) )
		{
			result = Long.valueOf( -1073741824 );
		}
		result = Long.valueOf( result.intValue() + 1 );
		sequenceCache.put( name, result );
		return result.intValue();
	}
}