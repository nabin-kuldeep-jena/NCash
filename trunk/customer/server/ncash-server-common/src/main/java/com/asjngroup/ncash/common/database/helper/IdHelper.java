package com.asjngroup.ncash.common.database.helper;


import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import java.util.HashMap;
import java.util.Map;

import com.asjngroup.ncash.common.database.hibernate.HibernateObject;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;


public class IdHelper
{
	private LongIdHelper longIdHelper;
	private static Map<Integer, Map<String, IdHelper>> idHelpers = new HashMap();

	private static final Map<String, Integer> idCache = new HashMap();
	private static final int STARTING_ID = -1073741824;

	public IdHelper( Class< ? > clazz, int blockSize )
	{
		this( clazz.getSimpleName(), blockSize );
	}

	public IdHelper( String objectKey, int blockSize )
	{
		if ( blockSize <= 0 )
		{
			throw new IllegalArgumentException( "Invalid block size " + blockSize + " passed to id helper" );
		}
		this.longIdHelper = new LongIdHelper( objectKey, blockSize );
	}

	public int getNextId() throws HibernateException
	{
		long nextLong = this.longIdHelper.getNextId();

		if ( nextLong >= 2147483647L )
		{
			throw new NCashRuntimeException( "id helper value has gone out of range. Current value is %1 max value is %2", new Object[]
			{ Long.valueOf( nextLong ), Integer.valueOf( 2147483647 ) } );
		}
		return ( int ) nextLong;
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
		Map map = ( Map ) idHelpers.get( stsIdKey );
		if ( map == null )
		{
			map = new HashMap();
			idHelpers.put( stsIdKey, map );
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

	public static int generateId( Class< ? > clazz )
	{
		return generateId( clazz, 1 );
	}

	public static int generateId( Class< ? > clazz, int required )
	{
		return new IdHelper( HibernateUtil.unproxyEntityClass( clazz ).getSimpleName(), required ).getNextId();
	}

	public static int generateId( String objectKey )
	{
		return generateId( objectKey, 1 );
	}

	public static int generateId( String objectKey, int required )
	{
		return new IdHelper( objectKey, required ).getNextId();
	}

	public static int getIdFor( Class< ? > clazz )
	{
		String name = clazz.getName();
		return getIdFor( name );
	}

	public static int getIdFor( String name )
	{
		Integer result = ( Integer ) idCache.get( name );
		if ( ( result == null ) || ( result.intValue() >= 0 ) )
		{
			result = Integer.valueOf( -1073741824 );
		}
		result = Integer.valueOf( result.intValue() + 1 );
		idCache.put( name, result );
		return result.intValue();
	}
}