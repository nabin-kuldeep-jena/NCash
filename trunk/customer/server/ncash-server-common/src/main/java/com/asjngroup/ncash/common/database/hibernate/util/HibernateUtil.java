package com.asjngroup.ncash.common.database.hibernate.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Entity;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.YesNoType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.database.helper.IdHelper;
import com.asjngroup.ncash.common.database.hibernate.HibernateObject;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class HibernateUtil
{
	private static final Log log = LogFactory.getLog( HibernateUtil.class );

	// class map for converting interface classes to their implementations
	private static Map<Class, Class> classMap = new HashMap<Class, Class>();

	// the reverse, implementation to interface
	private static Map<Class, Class> reverseClassMap = new HashMap<Class, Class>();

	public static Class< ? > unproxyEntityClass( Class< ? > clazz )
	{
		while ( clazz != null )
		{
			// normalise, useful for hibernate proxy objects
			if ( clazz.isAnnotationPresent( Entity.class ) )
			{
				return clazz;
			}

			clazz = clazz.getSuperclass();
		}

		return clazz;
	}

	public static SessionFactory buildSessionFactory( Configuration configuration )
	{
		return null;
	}

	public static <T> List<T> getAllObjects( SessionFactory sessionFactory, Class<T> clazz ) throws HibernateException
	{
		return HibernateUtil.find( sessionFactory, "from " + clazz.getName() );
	}

	public static List find( SessionFactory sessionFactory, String query ) throws HibernateException
	{
		final String queryF = query;

		List results = ( List ) doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				List resultSet = session.createQuery( queryF ).list();
				session.clear();
				return resultSet;
			}
		} );

		return results;
	}

	public static <T> T get( SessionFactory sessionFactory, final Class<T> clazz, Serializable id ) throws HibernateException
	{
		final Serializable idF = id;

		T result = ( T ) doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				T resultSet = ( T ) session.get( clazz, idF );
				session.clear();
				return resultSet;
			}
		} );

		return result;
	}

	public static Object doSessionWork( SessionFactory sessionFactory, SessionWorkListener listener ) throws HibernateException
	{
		return doSessionWork( sessionFactory, null, listener );
	}

	public static Object doSessionWork( SessionFactory sessionFactory, Interceptor interceptor, SessionWorkListener listener ) throws HibernateException
	{
		Session session = null;
		Object returnObj;

		try
		{
			if ( interceptor == null )
			{
				session = sessionFactory.openSession();
			}
			else
			{
				session = sessionFactory.withOptions().interceptor( interceptor ).openSession();
			}

			session.beginTransaction();
			returnObj = listener.doSessionWork( session );

			if ( session.getTransaction().isActive() )
			{
				session.getTransaction().commit();
			}
		}
		catch ( Exception e )
		{
			try
			{
				if ( session != null && session.getTransaction().isActive() )
				{
					session.getTransaction().rollback();
				}
			}
			catch ( HibernateException e1 )
			{
				log.error( "Error rolling back transaction", e1 );
			}
			throw new HibernateException( "Error attempting to do session work", e );
		}
		finally
		{
			try
			{
				if ( session != null )
				{
					session.close();
				}
			}
			catch ( HibernateException e )
			{
				log.error( "Error closing session", e );
			}
		}

		return returnObj;
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query ) throws HibernateException
	{
		return query( sessionFactory, query, new String[]
		{}, new Object[]
		{} );
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query, String paramName, Object paramValue ) throws HibernateException
	{
		return query( sessionFactory, query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		return query( sessionFactory, query, paramNames, paramValues, -1 );
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query, Object[] paramValues ) throws HibernateException
	{
		return query( sessionFactory, query, paramValues, -1 );
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query, Object[] paramValues, int maxResults ) throws HibernateException
	{
		final String queryF = query;
		final Object[] paramValuesF = paramValues;
		final int maxResultsF = maxResults;

		// shortcut the query if the max results is zero
		if ( maxResultsF == 0 )
		{
			return new ArrayList<T>();
		}

		List<T> results = ( List<T> ) doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				Query q = session.createQuery( queryF );

				setQueryParameters( q, paramValuesF );

				if ( maxResultsF > 0 )
				{
					q.setMaxResults( maxResultsF );
				}

				List retResults = q.list();
				session.clear();

				return retResults;
			}
		} );

		return results;
	}

	public static <T> List<T> query( Session session, String query, Object[] paramValues, int maxResults ) throws HibernateException
	{
		final String queryF = query;
		final Object[] paramValuesF = paramValues;
		final int maxResultsF = maxResults;

		// shortcut the query if the max results is zero
		if ( maxResultsF == 0 )
		{
			return new ArrayList<T>();
		}

		Query q = session.createQuery( queryF );

		setQueryParameters( q, paramValuesF );

		if ( maxResultsF > 0 )
		{
			q.setMaxResults( maxResultsF );
		}

		List<T> results = q.list();

		return results;
	}

	public static <T> List<T> query( SessionFactory sessionFactory, String query, String[] paramNames, Object[] paramValues, int maxResults ) throws HibernateException
	{
		final String queryF = query;
		final String[] paramNamesF = paramNames;
		final Object[] paramValuesF = paramValues;
		final int maxResultsF = maxResults;

		// shortcut the query if the max results is zero
		if ( maxResultsF == 0 )
		{
			return new ArrayList<T>();
		}

		List<T> results = ( List<T> ) doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				Query q = session.createQuery( queryF );

				setQueryParameters( q, paramNamesF, paramValuesF );

				if ( maxResultsF > 0 )
				{
					q.setMaxResults( maxResultsF );
				}

				List retResults = q.list();
				session.clear();

				return retResults;
			}
		} );

		return results;
	}

	public static <T> List<T> query( Session session, String query ) throws HibernateException
	{
		return query( session, query, new String[]
		{}, new Object[]
		{} );
	}

	public static <T> List<T> query( Session session, String query, String paramName, Object paramValue ) throws HibernateException
	{
		return query( session, query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> List<T> query( Session session, String query, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		return query( session, query, paramNames, paramValues, -1 );
	}

	public static <T> List<T> query( Session session, String query, Object[] paramValues ) throws HibernateException
	{
		return query( session, query, paramValues, -1 );
	}

	public static <T> List<T> query( Session session, String query, String[] paramNames, Object[] paramValues, int maxResults ) throws HibernateException
	{
		final String queryF = query;
		final String[] paramNamesF = paramNames;
		final Object[] paramValuesF = paramValues;
		final int maxResultsF = maxResults;

		// shortcut the query if the max results is zero
		if ( maxResultsF == 0 )
		{
			return new ArrayList<T>();
		}

		Query q = session.createQuery( queryF );

		setQueryParameters( q, paramNamesF, paramValuesF );

		if ( maxResultsF > 0 )
		{
			q.setMaxResults( maxResultsF );
		}

		List<T> results = q.list();

		return results;
	}

	static Object internalQueryExpectOneRow( SessionFactory sessionFactory, String query, String paramName[], Object paramValue[] ) throws HibernateException
	{
		List results = query( sessionFactory, query, paramName, paramValue );

		if ( results.size() == 0 )
		{
			throw new HibernateException( "Failed to get any rows, expected one" );
		}

		if ( results.size() > 1 )
		{
			throw new HibernateException( "Got mulitple rows, expected one" );
		}

		return results.get( 0 );
	}

	static Object internalQueryExpectOneRow( Session session, String query, String paramName[], Object paramValue[] ) throws HibernateException
	{
		List results = query( session, query, paramName, paramValue );

		if ( results.size() == 0 )
		{
			throw new HibernateException( "Failed to get any rows, expected one" );
		}

		if ( results.size() > 1 )
		{
			throw new HibernateException( "Got mulitple rows, expected one" );
		}

		return results.get( 0 );
	}

	public static <T> T queryExpectOneRow( Session session, String query ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( session, query, new String[]
		{}, new Object[]
		{} );
	}

	public static <T> T queryExpectOneRow( Session session, String query, String paramName, Object paramValue ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( session, query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> T queryExpectExactlyOneRow( Session session, String query, String paramName, Object paramValue ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( session, query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> T queryExpectOneRow( Session session, String query, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( session, query, paramNames, paramValues );
	}

	public static void setQueryParameters( Query q, Object[] paramValues ) throws HibernateException
	{
		for ( int i = 0; i < paramValues.length; i++ )
		{
			if ( paramValues[i] instanceof Boolean )
			{
				q.setParameter( i, paramValues[i], YesNoType.INSTANCE );
			}
			/*	else if ( paramValues[i] instanceof DateTime )
				{
					q.setParameter( i, paramValues[i], DateTimHibernate.custom( DateTimeType.class ) );
				}*/
			else
			{
				q.setParameter( i, paramValues[i] );
			}
		}
	}

	public static void setQueryParameters( Query q, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		// scan each parameter and substitute the correct type for special cases
		// such as yes_no
		// for the booleans and our custom date time type
		for ( int i = 0; i < paramNames.length; i++ )
		{
			if ( paramValues[i] instanceof Boolean )
			{
				q.setParameter( paramNames[i], paramValues[i], YesNoType.INSTANCE );
			}
			/*	else if ( paramValues[i] instanceof DateTime )
				{
					q.setParameter( paramNames[i], paramValues[i], Hibernate.custom( DateTimeType.class ) );
				}*/
			else if ( paramValues[i] instanceof Collection )
			{
				q.setParameterList( paramNames[i], ( Collection ) paramValues[i] );
			}
			/*	else if ( paramValues[i] instanceof BigDecimal )
				{
					q.setParameter( paramNames[i], paramValues[i], Hibernate.custom( DecimalType.class ) );
				}
				else if ( paramValues[i] instanceof String )
				{
					q.setString( paramNames[i], HibernateSession.getDataSource().escapeString( ( String ) paramValues[i] ) );
				}*/
			else
			{
				q.setParameter( paramNames[i], paramValues[i] );
			}
		}
	}

	public static <T extends HibernateObject> T createObject( Class<T> clazz, boolean allocateId ) throws HibernateException
	{
		T obj;

		// create an object
		try
		{
			obj = clazz.newInstance();
		}
		catch ( InstantiationException e )
		{
			throw new HibernateException( e );
		}
		catch ( IllegalAccessException e )
		{
			throw new HibernateException( e );
		}

		if ( allocateId )
		{
			// allocate an id
			IdHelper.allocateId( obj );
		}

		// return new object
		obj.setPartitionId( 1 );
		return obj;
	}

	public static <T extends HibernateObject> T createObject( Class<T> clazz, boolean allocateId, Integer partitionId ) throws HibernateException
	{
		T obj = createObject( clazz, allocateId );
		obj.setPartitionId( partitionId );
		return obj;
	}

	public static <T> T get( Session session, Class<T> clazz, Serializable id ) throws HibernateException
	{
		return ( T ) session.get( clazz, id );
	}

	public static <T> T copyObject( T orig )
	{
		T obj = null;
		try
		{
			// Write the object out to a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream( bos );
			out.writeObject( orig );
			out.flush();
			out.close();

			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( bos.toByteArray() ) );
			obj = ( T ) in.readObject();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		catch ( ClassNotFoundException cnfe )
		{
			cnfe.printStackTrace();
		}
		return obj;
	}

	public static Class getMappedInterfaceFromClass( Class type )
	{
		Class tClass = null;
		String packaze = type.getPackage().getName() + ".audit.";
		String clazz = null;
		//				ObjectHelper.getClassOnlyName( type ) + "Adt";

		try
		{
			tClass = Class.forName( packaze + clazz );
		}
		catch ( ClassNotFoundException e )
		{
			throw new NCashRuntimeException( e );
		}

		return tClass;
	}

	public static void update( SessionFactory sessionFactory, Object object ) throws HibernateException
	{
		final Object objectF = object;

		doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				session.update( objectF );
				return null;
			}
		} );
	}

	public static void update( SessionFactory sessionFactory, Object[] objects ) throws HibernateException
	{
		final Object[] objectsF = objects;

		doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				for ( Object object : objectsF )
					session.update( object );
				return null;
			}
		} );
	}

	public static void updateDatabase( SessionFactory sessionFactory, List< ? extends Object> savedObjects, List< ? extends Object> updatedObjects, List< ? extends Object> deletedObjects ) throws HibernateException
	{
		updateDatabase( sessionFactory, null, savedObjects, updatedObjects, deletedObjects );
	}

	public static void updateDatabase( SessionFactory sessionFactory, Interceptor interceptor, List< ? extends Object> savedObjects, List< ? extends Object> updatedObjects, List< ? extends Object> deletedObjects ) throws HibernateException
	{
		final List< ? extends Object> savedObjectsF = savedObjects;
		final List< ? extends Object> updatedObjectsF = updatedObjects;
		final List< ? extends Object> deletedObjectsF = deletedObjects;

		doSessionWork( sessionFactory, interceptor, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				updateDatabase( session, savedObjectsF, updatedObjectsF, deletedObjectsF );
				return null;
			}
		} );
	}

	public static void updateDatabase( Session session, List< ? extends Object> savedObjects, List< ? extends Object> updatedObjects, List< ? extends Object> deletedObjects ) throws HibernateException
	{
		// deletes first
		if ( deletedObjects != null )
		{
			for ( Object deleteObject : deletedObjects )
				session.delete( deleteObject );
		}

		// flush here so an object with a unique key can be removed and another
		// object with the same key added without causing the db to get upset
		session.flush();

		// saves/adds next
		if ( savedObjects != null )
		{
			for ( Object saveObject : savedObjects )
				session.save( saveObject );
		}

		// updates last
		if ( updatedObjects != null )
		{
			for ( Object updateObject : updatedObjects )
				session.update( updateObject );
		}
	}

	public static void save( SessionFactory sessionFactory, Object object ) throws HibernateException
	{
		final Object objectF = object;

		doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				session.save( objectF );
				return null;
			}
		} );
	}

	public static void save( SessionFactory sessionFactory, Object[] objects ) throws HibernateException
	{
		final Object[] objectsF = objects;

		doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				for ( Object object : objectsF )
					session.save( object );
				return null;
			}
		} );
	}

	public static void delete( SessionFactory sessionFactory, Object object ) throws HibernateException
	{
		final Object objectF = object;

		doSessionWork( sessionFactory, new SessionWorkListener()
		{
			public Object doSessionWork( Session session ) throws HibernateException
			{
				session.delete( objectF );
				return null;
			}
		} );
	}

	public static ClassMetadata getClassMetadata( SessionFactory sessionFactory, Class clazz ) throws HibernateException
	{
		Class mappedClass = getMappedClassFromInterface( clazz );

		return sessionFactory.getClassMetadata( mappedClass );
	}

	public static Class getMappedClassFromInterface( Class clazz )
	{
		// check the import map for an entry ( hack the generic slightly )
		Class toClazz = classMap.get( clazz );

		// if there isn't one just use the class passed in
		if ( toClazz == null )
		{
			toClazz = clazz;
		}

		return toClazz;
	}

	public static void addClassMaps( Map<Class, Class> newClassMap )
	{
		// add the forward maps
		classMap.putAll( newClassMap );

		// add the reverse maps
		for ( Map.Entry<Class, Class> entry : newClassMap.entrySet() )
		{
			reverseClassMap.put( entry.getValue(), entry.getKey() );
		}
	}
}
