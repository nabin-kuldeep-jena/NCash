package com.asjngroup.ncash.framework.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.dozer.CustomFieldMapper;
import org.dozer.DozerBeanMapper;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;

@SuppressWarnings( "unchecked" )
public class DozerService<H extends AbstractHibernateObject, D>
{
	private static DozerService dozerService;

	private static final int MAX_MAPPERS = 200;

	private static final int MAX_WAIT = -1;// keep waiting.

	private static final int MAX_IDLE = 200; // max objects lying in the pool.

	private static Log log = LogFactory.getLog( DozerService.class );

	private static GenericObjectPool dozerBeanMapperPool = new GenericObjectPool( new DozerBeanMapperFactory(), MAX_MAPPERS, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, MAX_WAIT, MAX_IDLE );

	private DozerService()
	{
	}

	private static DozerBeanMapper borrowMapper()
	{
		DozerBeanMapper mapper = null;
		try
		{
			mapper = ( DozerBeanMapper ) dozerBeanMapperPool.borrowObject();
		}
		catch ( Exception e )
		{
			log.error( e );
		}
		return mapper;
	}

	private static void returnMapper( DozerBeanMapper dozerBeanMapper )
	{
		try
		{
			dozerBeanMapperPool.returnObject( dozerBeanMapper );
		}
		catch ( Exception e )
		{
			log.error( e );
		}
	}

	public static DozerService getService()
	{
		if ( dozerService == null )
		{
			dozerService = new DozerService();
		}
		return dozerService;
	}

	public H toHibernateObject( D dtoObj, Class<H> hibernateObjClass )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			return ( H ) dozerBeanMapper.map( dtoObj, hibernateObjClass );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}

	}

	public Object toObject( H hibernateObject, Class< ? > clazz )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			return dozerBeanMapper.map( hibernateObject, clazz );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public void toHibernateObject( D dtoObj, H hibernateObj )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			dozerBeanMapper.map( dtoObj, hibernateObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public List<H> toHibernateObjects( List<D> dtoObjects, Class<H> hibernateObjClass )
	{
		List hibernateObjs = new ArrayList<H>();
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			for ( int i = 0; i < dtoObjects.size(); i++ )
				hibernateObjs.add( dozerBeanMapper.map( dtoObjects.get( i ), hibernateObjClass ) );
			return hibernateObjs;
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public List convertObjects( List objects, Class clazz )
	{
		DozerBeanMapper dozerBeanMapper = null;
		List convertedObjects = new ArrayList();
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			for ( Object obj : objects )
				convertedObjects.add( dozerBeanMapper.map( obj, clazz ) );
			return convertedObjects;
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public void toDTOObject( H fromObject, D dtoObj )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			dozerBeanMapper.map( fromObject, dtoObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public Object toDTOObjectGeneric( Object fromObject, Class dtoClass )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			return dozerBeanMapper.map( fromObject, dtoClass );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public D toDTOObject( H hibernateObject, Class<D> dtoClass )
	{
		return toDTOObject( hibernateObject, dtoClass, true );
	}

	public D toDTOObject( H hibernateObject, Class<D> dtoClass, boolean customInternationaliser )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			return ( D ) dozerBeanMapper.map( hibernateObject, dtoClass );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	@Deprecated
	/*
	 *  This method is to be removed as it does not match framework
	 */
	/*public D toDTOObject( Class<D> dtoClass, RMISerializable temp )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			return ( D ) dozerBeanMapper.map( temp, dtoClass );
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}*/

	public List<D> toDTOObjects( List<H> hibernateObjects, Class<D> dtoClass )
	{
		List dtoObjs = new ArrayList<D>();
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			for ( int i = 0; i < hibernateObjects.size(); i++ )
				dtoObjs.add( dozerBeanMapper.map( hibernateObjects.get( i ), dtoClass ) );
			return dtoObjs;
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public List<D> toDTOObjects( Collection<H> hibernateObjects, Class<D> dtoClass )
	{
		List dtoObjs = new ArrayList<D>();
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			for ( H object : hibernateObjects )
				dtoObjs.add( dozerBeanMapper.map( object, dtoClass ) );
			return dtoObjs;
		}
		finally
		{
			if ( dozerBeanMapper != null )
				DozerService.returnMapper( dozerBeanMapper );
		}
	}

	public H toHibernateObject( D dtoObj, Class<H> hibernateObjClass, boolean customWrapper )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl() );
			H hibObject = ( H ) dozerBeanMapper.map( dtoObj, hibernateObjClass );
			return hibObject;
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public void toHibernateObject( D dtoObj, H hibernateObj, boolean customWrapper )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl() );
			dozerBeanMapper.map( dtoObj, hibernateObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public H toHibernateObject( D dtoObj, Class<H> hibernateObjClass, CustomFieldMapper customWrapper )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper != null )
				dozerBeanMapper.setCustomFieldMapper( customWrapper );
			H hibObject = ( H ) dozerBeanMapper.map( dtoObj, hibernateObjClass );
			return hibObject;
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public void toHibernateObject( D dtoObj, H hibernateObj, CustomFieldMapper customWrapper )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper != null )
				dozerBeanMapper.setCustomFieldMapper( customWrapper );
			dozerBeanMapper.map( dtoObj, hibernateObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public H toHibernateObject( D dtoObj, Class<H> hibernateObjClass, boolean customWrapper, Session session )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl( session ) );
			H hibObject = ( H ) dozerBeanMapper.map( dtoObj, hibernateObjClass );
			return hibObject;
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public void toHibernateObject( D dtoObj, H hibernateObj, boolean customWrapper, Session session )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl( session ) );

			dozerBeanMapper.map( dtoObj, hibernateObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public H toHibernateObject( D dtoObj, Class<H> hibernateObjClass, boolean customWrapper, boolean blockCascade )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl( blockCascade ) );
			H hibObject = ( H ) dozerBeanMapper.map( dtoObj, hibernateObjClass );
			return hibObject;
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	public void toHibernateObject( D dtoObj, H hibernateObj, boolean customWrapper, boolean blockCascade )
	{
		DozerBeanMapper dozerBeanMapper = null;
		try
		{
			dozerBeanMapper = DozerService.borrowMapper();
			if ( customWrapper )
				dozerBeanMapper.setCustomFieldMapper( new CustomFieldMapperImpl( blockCascade ) );
			dozerBeanMapper.map( dtoObj, hibernateObj );
		}
		finally
		{
			if ( dozerBeanMapper != null )
			{
				dozerBeanMapper.setCustomFieldMapper( null );
				DozerService.returnMapper( dozerBeanMapper );
			}
		}
	}

	// Factory to create the DozerBeanMapper,
	private static class DozerBeanMapperFactory extends BasePoolableObjectFactory
	{
		@Override
		public Object makeObject() throws Exception
		{
			DozerBeanMapper mapper = new DozerBeanMapper();
			List dozerBeanMappingFiles = new ArrayList();
			dozerBeanMappingFiles.add( "sparkDozerBeanMapping.xml" );
			mapper.setMappingFiles( dozerBeanMappingFiles );
			return mapper;
		}
	}
}