/**
 * 
 */
package com.asjngroup.ncash.common.database.hibernate.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.asjngroup.ncash.common.ObjectHelper;
import com.asjngroup.ncash.common.database.helper.IdGenerator;
import com.asjngroup.ncash.common.database.hibernate.HibernateObject;
import com.asjngroup.ncash.common.database.hibernate.exception.HibernateUtilException;
import com.asjngroup.ncash.common.models.UserSession;

/**
 * @author nabin.jena
 *
 */
public class HibernateSession
{
	private static final Log LOG = LogFactory.getLog( HibernateSession.class );

	private static final Integer SYSTEM_PARTITION = 0;

	private static final Integer COMMON_PARTITION = 1;

	private static HibernateInfo hibernateInfo;

	private static boolean initialised = false;

	private static Map<String, Class> classNameInterfaceMap;

	private static boolean isCommonPartitionAccess = true;

	private HibernateSession()
	{

	}

	public static HibernateSession initialise( Configuration configuration, SessionFactory factory, DataSource dataSource, IdGenerator idGenerator ) throws HibernateUtilException
	{
		hibernateInfo = new HibernateInfo( factory, dataSource, idGenerator );
		HibernateInfo.setConfiguration( configuration );
		initialised = true;

		return null;
	}

	public static HibernateSession initialise( HibernateInfo hibInfo ) throws HibernateUtilException
	{
		hibernateInfo = hibInfo;

		// build an import map for object creation
		Map<Class, Class> importMap = HibernateConfigurationHelper.buildImportMap( hibInfo.getConfiguration() );
		classNameInterfaceMap = HibernateConfigurationHelper.buildClassNameInterfaceMap( importMap );

		initialised = true;

		return null;
	}

	public static void cleanup()
	{
		if ( !initialised )
			return;

		// Close the session factory
		try
		{
			if ( getSessionFactory() != null )
				getSessionFactory().close();
		}
		catch ( HibernateException e )
		{
		}
	}

	public static Class getInterfaceFromName( String objectName )
	{
		return classNameInterfaceMap.get( objectName );
	}

	public static SessionFactory getSessionFactory()
	{
		// Return the session factory
		checkInitialised();
		return hibernateInfo.getSessionFactory();
	}

	public static DataSource getDataSource()
	{
		// Return the session factory
		checkInitialised();
		return hibernateInfo.getDataSource();
	}

	public static IdGenerator getIdGenerator()
	{
		// Return the id generator
		checkInitialised();
		return null;
		//	hibernateInfo.getIdGenerator();
	}

	private static void checkInitialised()
	{
		// Make sure the class has been initialised
		if ( !initialised )
			throw new IllegalStateException();
	}

	public static <T> List<T> find( String query ) throws HibernateException
	{
		return HibernateUtil.find( getSessionFactory(), query );
	}

	public static <T> List<T> query( String query, String paramName, Object paramValue ) throws HibernateException
	{
		return ( List<T> ) HibernateUtil.query( getSessionFactory(), query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> List<T> query( String query, String paramName, Object paramValue, int maxResults ) throws HibernateException
	{
		return ( List<T> ) HibernateUtil.query( getSessionFactory(), query, new String[]
		{ paramName }, new Object[]
		{ paramValue }, maxResults );
	}

	public static <T> List<T> query( String query, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		return ( List<T> ) HibernateUtil.query( getSessionFactory(), query, paramNames, paramValues, -1 );
	}

	public static <T> List<T> query( String query, String[] paramNames, Object[] paramValues, int maxResults ) throws HibernateException
	{
		return ( List<T> ) HibernateUtil.query( getSessionFactory(), query, paramNames, paramValues, maxResults );
	}

	public static <T> List<T> query( String query, Object[] paramValues ) throws HibernateException
	{
		return ( List<T> ) HibernateUtil.query( getSessionFactory(), query, paramValues, -1 );
	}

	public static <T> List<T> query( String query, Object[] paramValues, int maxResults ) throws HibernateException
	{
		return HibernateUtil.query( getSessionFactory(), query, paramValues, maxResults );
	}

	public static <T> T queryExpectOneRow( String query ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( getSessionFactory(), query, new String[]
		{}, new Object[]
		{} );
	}

	public static <T> T queryExpectOneRow( String query, String paramName, Object paramValue ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( getSessionFactory(), query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> T queryExpectExactlyOneRow( String query, String paramName, Object paramValue ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( getSessionFactory(), query, new String[]
		{ paramName }, new Object[]
		{ paramValue } );
	}

	public static <T> T queryExpectOneRow( String query, String[] paramNames, Object[] paramValues ) throws HibernateException
	{
		return ( T ) HibernateUtil.internalQueryExpectOneRow( getSessionFactory(), query, paramNames, paramValues );
	}

	public static <T> T get( Class<T> clazz, Serializable id ) throws HibernateException
	{
		return HibernateUtil.get( getSessionFactory(), clazz, id );
	}

	public static <T> List<T> getAllObjects( Class<T> clazz ) throws HibernateException
	{
		return HibernateUtil.getAllObjects( getSessionFactory(), clazz );
	}

	public static <T extends HibernateObject> T createObject( Class<T> clazz ) throws HibernateException
	{
		return createObject( clazz, true );
	}

	public static <T extends HibernateObject> T createObject( Class<T> clazz, boolean allocateId ) throws HibernateException
	{
		return HibernateUtil.createObject( clazz, allocateId );
	}

	public static <T> T instantiate( Class<T> clazz ) throws HibernateException
	{
		throw new HibernateException( "Method not implemented" );//	return HibernateUtil.instantiate( clazz );
	}

	public static void save( Object obj ) throws HibernateException
	{
		HibernateUtil.save( getSessionFactory(), obj );
	}

	public static void save( Object[] obj ) throws HibernateException
	{
		HibernateUtil.save( getSessionFactory(), obj );
	}

	public static void update( Object obj ) throws HibernateException
	{
		HibernateUtil.update( getSessionFactory(), obj );
	}

	public static void update( Object[] obj ) throws HibernateException
	{
		HibernateUtil.update( getSessionFactory(), obj );
	}

	public static void delete( Object obj ) throws HibernateException
	{
		HibernateUtil.delete( getSessionFactory(), obj );
	}

	public static void delete( Object[] objs ) throws HibernateException
	{
		HibernateUtil.delete( getSessionFactory(), objs );
	}

	public static void delete( String query ) throws HibernateException
	{
		HibernateUtil.delete( getSessionFactory(), query );
	}

	public static void delete( String query, Object[] params ) throws HibernateException
	{
		//HibernateUtil.delete( getSessionFactory(), query, params );
	}

	public static void updateDatabase( List< ? extends Object> savedObjects, List< ? extends Object> updatedObjects, List< ? extends Object> deletedObjects ) throws HibernateException
	{
		HibernateUtil.updateDatabase( getSessionFactory(), savedObjects, updatedObjects, deletedObjects );
	}

	public static Integer generateId( String objectKey, int required ) throws HibernateException
	{
		return hibernateInfo.getIdGenerator().generate( objectKey, required );
	}

	public static Integer generateId( String objectKey ) throws HibernateException
	{
		return generateId( objectKey, 1 );
	}

	public static Integer generateId( Class clazz, int required ) throws HibernateException
	{
		return generateId( getIdObjectKeyFromClass( clazz ), required );
	}

	public static Integer generateId( Class clazz ) throws HibernateException
	{
		return generateId( clazz, 1 );
	}

	public static Long generateLongId( String objectKey, long required ) throws HibernateException
	{
		return hibernateInfo.getIdGenerator().generateLong( objectKey, required );
	}

	public static Long generateLongId( String objectKey ) throws HibernateException
	{
		return generateLongId( objectKey, 1 );
	}

	public static Long generateLongId( Class clazz, int required ) throws HibernateException
	{
		return generateLongId( getIdObjectKeyFromClass( clazz ), required );
	}

	public static Long generateLongId( Class clazz ) throws HibernateException
	{
		return generateLongId( clazz, 1 );
	}

	public static String getIdObjectKeyFromClass( Class clazz )
	{
		// get the interface class
		Class toClazz = HibernateUtil.getMappedInterfaceFromClass( clazz );
		return ObjectHelper.getClassOnlyName( toClazz );
	}

	public static Session openSessionWithTransaction()
	{
		Session session = getSession( -1 );
		session.getTransaction().begin();
		return session;
	}

	public static void closeSessionWithTransaction( Session session )
	{
		try
		{
			if ( session.getTransaction().isActive() )
			{
				session.getTransaction().rollback();
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		try
		{
			session.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static Session openSession( UserSession userSession )
	{
		return openSession( userSession, true );
	}

	public static Session openSession( UserSession userSession, boolean partFilter )
	{
		Session session = openSession( userSession.getUserTbl().getUsrId() );
		if ( partFilter )
			enablePartitionFilterWithCommonCheck( session, userSession );
		return session;
	}

	public static Session openSession( int id )
	{
		Session session = getSession( id );
		enableDeletedFilter( session );
		return session;
	}

	private static Session getSession( int id )
	{
		Session session = null;
		if ( id > -1 )
		{
			//ChangedClassInterceptor interceptor = new ChangedClassInterceptor();
			session = HibernateSession.getSessionFactory().openSession( /*interceptor*/ );
			/*interceptor.setSession( session );
			interceptor.setUserId( id );*/
		}
		else
		{
			session = HibernateSession.getSessionFactory().openSession();
		}

		return session;
	}

	public static void enableDeletedFilter( Session pSession )
	{
		pSession.enableFilter( "deletedFilter" ).setParameter( "deleteFl", Boolean.FALSE );
	}

	public static void enablePartitionFilterWithCommonCheck( Session pSession, UserSession userSession )
	{
		enablePartitionBasedFilter( pSession, userSession, true );
	}

	private static void enablePartitionBasedFilter( Session pSession, UserSession userSession, boolean withCommonCheck )
	{
		List<Integer> partitionIds = new ArrayList<Integer>();
		partitionIds.add( SYSTEM_PARTITION );
		for ( Integer id : userSession.getUserPartitionIDs() )
		{
			partitionIds.add( id );
		}

		if ( withCommonCheck && !partitionIds.contains( COMMON_PARTITION ) && isCommonPartitionAccess )
		{
			partitionIds.add( COMMON_PARTITION );
		}

		pSession.enableFilter( "partitionFilter" ).setParameterList( "partitionIds", partitionIds );
	}

	public static void closeSession( Session session )
	{
		try
		{
			if ( null == session )
				return;

			if ( session.getTransaction().isActive() )
			{
				session.getTransaction().rollback();
			}
		}
		catch ( Exception e )
		{
			LOG.error( "Error rolling back active transaction", e );
		}

		try
		{
			if ( session.isOpen() )
				session.close();
		}
		catch ( Exception e )
		{
			LOG.error( "Error closing session", e );
		}
	}

	public static Session openReadOnlySession()
	{
		Session session = HibernateSession.getSessionFactory().openSession();
		enableDeletedFilter( session );
		return session;
	}

	public static Session openSession()
	{
		Session session = getSession( -1 );
		enableDeletedFilter( session );
		return session;
	}

	public static void enablePartitionFilter( Session pSession, UserSession userSession )
	{
		enablePartitionBasedFilter( pSession, userSession, false );
	}

	public static void disablePartitionFilter( Session pSession )
	{
		pSession.disableFilter( "partitionFilter" );
	}

	public static Session openSessionWithoutFilter()
	{
		return getSession( -1 );
	}

	public static Session openSessionWithPartitionFilter( UserSession userSession )
	{
		Session session = getSession( -1 );
		enablePartitionFilter( session, userSession );
		return session;
	}

	public static void disableDeletedFilter( Session pSession )
	{
		pSession.disableFilter( "deletedFilter" );
	}

	public static Configuration getConfiguration()
	{
		return hibernateInfo.getConfiguration();
	}

	/*public static Session openSession( int userId, AuditSession auditSession )
	{
		Session session = getSession( userId, auditSession, null, true );
		enableDeletedFilter( session );
		return session;
	}
	
	public static Session openSession( int userId, AuditTrailConverter auditTrail, AuditSession auditSession )
	{
		Session session = getSession( userId, auditSession, auditTrail, true );
		enableDeletedFilter( session );
		return session;
	}
	
	public static Session openSession( int userId, AuditTrailConverter auditTrail, AuditSession auditSession, Boolean handleExtraArgs )
	{
		Session session = getSession( userId, auditSession, auditTrail, handleExtraArgs );
		enableDeletedFilter( session );
		return session;
	}
	
	private static Session getSession( int id, AuditSession auditSession, AuditTrailConverter auditTrail, Boolean handleExtraArgs )
	{
		Session session = null;
	
		if ( id > -1 )
		{
			AuditLoggerInterceptor interceptor = new AuditLoggerInterceptor();
			session = HibernateSession.getSessionFactory().openSession( interceptor );
			interceptor.setUserId( id );
			interceptor.initialiseLogger( session, auditSession, auditTrail );
			interceptor.setHandleExtraArgs( handleExtraArgs );
		}
		else
		{
			session = HibernateSession.getSessionFactory().openSession();
		}
	
		session.getTransaction().begin();
		return session;
	}*/

}
