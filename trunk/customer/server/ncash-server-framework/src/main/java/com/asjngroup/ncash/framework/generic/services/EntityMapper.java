
package com.asjngroup.ncash.framework.generic.services;

import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ClassUtils;

import com.asjngroup.ncash.common.database.helper.IdHelper;
import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;
import com.asjngroup.ncash.common.database.hibernate.HibernateObject;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.properties.EnvironmentProperties;
import com.asjngroup.ncash.common.properties.NCashServerProperties;
import com.asjngroup.ncash.common.properties.NCashSystemPropertes;
import com.asjngroup.ncash.common.util.ReflectionUtils;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapper;
import com.asjngroup.ncash.framework.generic.models.EntityLink;
import com.asjngroup.ncash.framework.generic.models.EntityModel;
import com.asjngroup.ncash.framework.generic.models.EntityRef;

import io.swagger.models.Model;

public class EntityMapper
{
	static int count = 0;
	final String PUT = "PUT";
	private Map<String, List<String>> skipPropertyConfig;

	private Map<String, ClassMetadata> metaDataMap = new HashMap<>();

	public EntityMapper()
	{
		init();
	}

	// FIXME: A restart of the web server will be required for repopulating
	// this. So dynamic entities, if supported, will not be handled
	private void init()
	{
		// get the class meta data with entity names
		@SuppressWarnings( "unchecked" )
		Map<String, ClassMetadata> allData = ( Map<String, ClassMetadata> ) HibernateSession.getSessionFactory().getAllClassMetadata();
		for ( String entityName : allData.keySet() )
		{
			String key = entityName.substring( entityName.lastIndexOf( "." ) + 1 );
			if ( metaDataMap.put( key, allData.get( entityName ) ) != null )
			{
				System.out.println( "Duplicates found for " + entityName );
			}
		}
	}

	public Object map( EntityModel entityModel, Session session ) throws Exception
	{

		ClassMetadata clsMetadata = metaDataMap.get( entityModel.getEntityName() );
		if ( clsMetadata == null )
		{
			throw new Exception( "Unsupported Entity :" + entityModel.getEntityName() );
		}

		Class< ? > entClaz = Class.forName( clsMetadata.getEntityName() );
		Object obj = entClaz.newInstance();
		int id = IdHelper.getIdFor( entClaz );

		mapProperties( obj, entityModel, session, null );
		( ( AbstractHibernateObject ) obj ).setId( id );

		return obj;
	}

	public void mapProperties( Object obj, EntityModel model, Session session, String requestType ) throws Exception
	{
		// create a property descriptor map
		PropertyDescriptor[] pds = Introspector.getBeanInfo( obj.getClass() ).getPropertyDescriptors();
		Map<String, PropertyDescriptor> pdMap = new HashMap<String, PropertyDescriptor>();
		for ( PropertyDescriptor pd : pds )
		{
			pdMap.put( pd.getName(), pd );
		}

		for ( String propName : model.getProperties().keySet() )
		{
			// set the normal properties,
			PropertyDescriptor pd = pdMap.get( propName );
			if ( pd == null )
			{
				throw new Exception( "The property " + propName + " is not part of the entity " + model.getEntityName() );
			}

			Object propValue = model.getProperties().get( propName );
			Class< ? > propertyType = pd.getPropertyType();

			if ( pd.getPropertyType().isAssignableFrom( BigDecimal.class ) )
			{
				BigDecimal db = new BigDecimal( propValue.toString() );
				db.setScale( EnvironmentProperties.getNcashSystemPropertes().getNcashSvrSystemDp(), RoundingMode.HALF_UP );
				propValue = db;
			}
			else if ( pd.getPropertyType().isAssignableFrom( org.joda.time.DateTime.class ) )
			{
				propValue = new org.joda.time.DateTime( propValue );
			}

			System.out.println( propName );
			Method writeMethod = pd.getWriteMethod();
			if ( AbstractHibernateObject.class.isAssignableFrom( pd.getPropertyType() ) )
			{
				@SuppressWarnings( "rawtypes" )
				Map propValueMap = ( Map ) propValue;

				if ( "PUT".equalsIgnoreCase( requestType ) )
				{
					Object nestedId = propValueMap.get( "id" );
					if ( nestedId != null )
					{
						propValue = session.get( pd.getPropertyType(), Integer.valueOf( nestedId.toString() ) );
						if ( propValue == null )
							throw new NullPointerException( "Id Not Found!!" );
					}
					writeMethod.invoke( obj, propValue );
				}
				else
				{
					Object nestedId = propValueMap.get( "id" );
					if ( nestedId != null )
					{
						propValue = session.get( pd.getPropertyType(), Integer.valueOf( nestedId.toString() ) );
						if ( propValue == null )
							throw new NullPointerException( "Id Not Found!!" );

						writeMethod.invoke( obj, propValue );
					}
					else
					{
						Object nestedObj = propertyType.newInstance();
						int id = IdHelper.getIdFor( propertyType );
						( ( AbstractHibernateObject ) nestedObj ).setId( id );
						PropertyDescriptor[] nestedPds = Introspector.getBeanInfo( propertyType ).getPropertyDescriptors();
						for ( PropertyDescriptor nestedPd : nestedPds )
						{
							Method nestedWriteMethod = nestedPd.getWriteMethod();
							if ( nestedWriteMethod != null )
							{
								if ( propValueMap.get( nestedPd.getName() ) != null )
								{
									nestedWriteMethod.invoke( nestedObj, propValueMap.get( nestedPd.getName() ) );
								}
							}
						}
						writeMethod.invoke( obj, nestedObj );
						session.save( nestedObj );
					}
				}
				/*
				 * else { PropertyDescriptor[] nestedPds =
				 * Introspector.getBeanInfo( propertyType
				 * ).getPropertyDescriptors();
				 * 
				 * Object nestedObj = propertyType.newInstance(); int id =
				 * IdHelper.getIdFor( propertyType ); ( (
				 * AbstractHibernateObject ) nestedObj ).setId( id );
				 * 
				 * for ( PropertyDescriptor nestedPd : nestedPds ) { Method
				 * nestedWriteMethod = nestedPd.getWriteMethod(); if (
				 * nestedWriteMethod != null ) { if ( propValueMap.get(
				 * nestedPd.getName() ) != null ) { nestedWriteMethod.invoke(
				 * nestedObj, propValueMap.get( nestedPd.getName() ) ); } } }
				 * writeMethod.invoke( obj, nestedObj ); session.save( nestedObj
				 * ); }
				 */

			}
			/*
			 * else if ( propValue instanceof EntityRef ) { EntityRef ref = (
			 * EntityRef ) propValue; propValue = session.get( Class.forName(
			 * metaDataMap.get( ref.getEntityName() ).getEntityName() ),
			 * Integer.parseInt( ref.getId() ) ); }
			 */

			// Method writeMethod = pd.getWriteMethod();
			else if ( writeMethod != null )
			{
				/*
				 * String[] tablePrefix = propName.split( "Id" ); String
				 * nestedObjId = propName.substring( propName.length() - 2 );
				 * Object dummyNestedObj = null; if ( "Id".equals( nestedObjId )
				 * ) dummyNestedObj = GenericHelper.checkIfNestedObjId( session,
				 * tablePrefix[0], propValue );
				 */
				/* HibernateSession.createObject( dummyNestedObj.getClass() ) */

				// writeMethod.invoke( obj, propValue );
				writeMethod.invoke( obj, propValue );

			}
		}
	}

	// TODO: use the meta data instead of pure reflection to do the mapping
	/**
	 * @param obj
	 * @return
	 * @throws Exception
	 * @{@link Deprecated} This method uses pure reflection, which might result
	 *         in some unwanted fields coming in the entity response
	 */
	public EntityModel map_old( Object obj ) throws Exception
	{
		EntityModel model = new EntityModel();
		model.setEntityName( obj.getClass().getSimpleName() );

		PropertyDescriptor[] pd = Introspector.getBeanInfo( obj.getClass() ).getPropertyDescriptors();
		for ( PropertyDescriptor prop : pd )
		{

			Method readMethod = prop.getReadMethod();
			if ( readMethod == null || skipProperty( model.getEntityName(), prop.getName() ) )
			{
				// skip the ones without a getter, and with a skip configuration
				continue;
			}

			Object propValue = readMethod.invoke( obj );
			if ( "id".equalsIgnoreCase( prop.getName() ) )
			{
				model.setId( String.valueOf( propValue ) );
				continue;
			}

			if ( !isPrimitive( prop.getPropertyType() ) )
			{
				// check if it is a collection
				if ( prop.getPropertyType().equals( Collection.class ) )
				{
					// add links to non primitives
					if ( propValue != null )
					{
						Collection< ? > collection = ( Collection< ? > ) propValue;
						for ( Object colObj : collection )
						{
							model.addLink( mapLink( colObj ) );
						}
					}

				}
				else
				{
					if ( propValue != null )
					{
						if ( propValue instanceof HibernateObject )
						{
							addPropertyAndLink( model, prop.getName(), propValue, false );
						}
						else
						{
							model.addProperty( prop.getName(), /* String.valueOf( */propValue/* ) */ );
						}
					}
				}
			}
			else
			{
				// primitive type
				model.addProperty( prop.getName(), propValue );
			}
		}
		return model;
	}

	public EntityModel map( Object obj, Set<String> nestedObjects, boolean isLinkReq ) throws Exception
	{
		EntityModel model = new EntityModel();
		String simpleName = obj.getClass().getSimpleName();
		model.setEntityName( simpleName );
		ClassMetadata metadata = metaDataMap.get( model.getEntityName() );
		if ( !nestedObjects.add( simpleName ) )
			return null;
		if ( metadata == null )
		{
			throw new Exception( "Unsupported Entity :" + model.getEntityName() );
		}
		model.setId( String.valueOf( metadata.getIdentifier( obj ) ) );

		for ( String propName : metadata.getPropertyNames() )
		{
			Object propValue = metadata.getPropertyValue( obj, propName );
			Type type = metadata.getPropertyType( propName );
			Class< ? > propClass = type.getReturnedClass();

			// Field[] modelFields = propValue.getClass().getDeclaredFields();
			if ( !isPrimitive( propClass ) )
			{
				// check if it is a collection
				if ( propClass.equals( Collection.class ) )
				{
					// add links to non primitives
					if ( propValue != null && isLinkReq )
					{
						Collection< ? > collection = ( Collection< ? > ) propValue;
						for ( Object colObj : collection )
						{
							model.addLink( mapLink( colObj ) );
						}
					}

				}
				else
				{
					if ( propValue != null )
					{

						if ( propValue instanceof HibernateObject )
						{
							EntityModel nestedModel = map( propValue, nestedObjects, isLinkReq );
							model.addProperty( propName, nestedModel.getProperties() );

						}
						else if ( propValue instanceof HibernateObject )
						{

							addPropertyAndLink( model, propName, propValue, isLinkReq );
						}
						else
						{
							model.addProperty( propName, /* String.valueOf( */propValue/* ) */ );
						}
					}
				}
			}
			else
			{
				// primitive type
				model.addProperty( propName, propValue );
			}
		}
		return model;
	}

	public EntityModel map( Object obj, CustomizeDataMapper customizeDataMapper,Set<String> nestedObjects, boolean isLinkReq ) throws Exception
	{
		EntityModel model = new EntityModel();
		model.setEntityName( obj.getClass().getSimpleName() );
		ClassMetadata metadata = metaDataMap.get( model.getEntityName() );
		if ( metadata == null )
		{
			throw new Exception( "Unsupported Entity :" + model.getEntityName() );
		}
		model.setId( String.valueOf( metadata.getIdentifier( obj ) ) );
		for ( String propName : metadata.getPropertyNames() )
		{
			Object propValue = metadata.getPropertyValue( obj, propName );
			Type type = metadata.getPropertyType( propName );
			Class< ? > propClass = type.getReturnedClass();

			// Field[] modelFields = propValue.getClass().getDeclaredFields();
			if ( !isPrimitive( propClass ) )
			{
				// check if it is a collection
				if ( propClass.equals( Collection.class ) )
				{
					// add links to non primitives
					if ( propValue != null && isLinkReq )
					{
						Collection< ? > collection = ( Collection< ? > ) propValue;
						for ( Object colObj : collection )
						{
							model.addLink( mapLink( colObj ) );
						}
					}

				}
				else
				{
					if ( propValue != null )
					{

						if ( propValue instanceof HibernateObject )
						{
							EntityModel nestedModel = map( propValue, nestedObjects, isLinkReq );
							model.addProperty( propName, nestedModel.getProperties() );

						}
						else if ( propValue instanceof HibernateObject )
						{

							addPropertyAndLink( model, propName, propValue, isLinkReq );
						}
						else
						{
							model.addProperty( propName, /* String.valueOf( */propValue/* ) */ );
						}
					}
				}
			}
			else
			{
				// primitive type
				model.addProperty( propName, propValue );
			}
		}
		if ( customizeDataMapper != null )
			customizeDataMapper.mapCutomeData( obj, metadata, model );
		return model;
	}

	private Map<String, Object> fetchNestedObjs( Object propValue, String entityName, String id ) throws Exception
	{
		Map<String, Object> modelPropertyCols = new HashMap<>();

		Class< ? extends Object> propertyClass = propValue.getClass();
		for ( Field field : propertyClass.getDeclaredFields() )
		{
			boolean flag = true;

			if ( "serialVersionUID".equals( field.getName() ) )
				continue;

			String methodName = "get" + StringHelper.upperFirstChar( field.getName() );
			Method method = ReflectionUtils.getMethod( propertyClass, methodName );
			Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
			for ( Annotation annotation : declaredAnnotations )
			{
				if ( annotation.annotationType().toString().contains( "javax.persistence.ManyToOne" ) || annotation.annotationType().toString().contains( "javax.persistence.OneToMany" ) || annotation.annotationType().toString().contains( "javax.persistence.OneToOne" ) )
				{
					flag = false;
					// System.out.println( "Nested : " + field.getName() );
					break;
				}

			}

			modelPropertyCols.put( "entityName", entityName );
			modelPropertyCols.put( "id", id );

			if ( flag )
			{
				Object nestedValue = ReflectionUtils.invoke( method, propValue, null );
				modelPropertyCols.put( field.getName(), nestedValue );
			}

		}

		return modelPropertyCols;

	}

	private void addPropertyAndLink( EntityModel model, String propName, Object propValue, boolean isLink ) throws Exception
	{
		String entityName = propValue.getClass().getSimpleName();
		String id = String.valueOf( new PropertyDescriptor( "id", propValue.getClass() ).getReadMethod().invoke( propValue ) );
		if ( isLink )
			model.addLink( createLink( propName, entityName, id ) );
		// model.addProperty( propName, createRef( entityName, id ) );
		model.addProperty( propName, fetchNestedObjs( propValue, entityName, id ) );
	}

	public EntityRef mapRef( Object obj ) throws Exception
	{
		String entityName = obj.getClass().getSimpleName();
		String id = String.valueOf( new PropertyDescriptor( "id", obj.getClass() ).getReadMethod().invoke( obj ) );
		return createRef( entityName, id );
	}

	private EntityRef createRef( String entityName, String id )
	{
		EntityRef ref = new EntityRef();
		ref.setEntityName( entityName );
		ref.setId( id );
		return ref;
	}

	private boolean skipProperty( String entityName, String propertyName )
	{
		List<String> skipProperties = skipPropertyConfig.get( "*" );
		if ( skipPropertyConfig.containsKey( entityName ) )
		{
			skipProperties.addAll( skipPropertyConfig.get( entityName ) );
		}
		return skipProperties.contains( propertyName );
	}

	public EntityLink createLink( String entityName, String id )
	{
		return createLink( entityName + "-" + id, entityName, id );
	}

	public EntityLink createLink( String rel, String entityName, String id )
	{
		EntityLink link = new EntityLink();
		link.setRel( rel );
		link.setHref( "objects/" + entityName + "/" + id );
		return link;

	}

	public EntityLink mapLink( Object obj ) throws Exception
	{
		EntityLink link = null;
		String entityName = obj.getClass().getSimpleName();
		PropertyDescriptor idProp = new PropertyDescriptor( "id", obj.getClass() );
		link = createLink( entityName, String.valueOf( idProp.getReadMethod().invoke( obj ) ) );
		return link;
	}

	private boolean isPrimitive( Class< ? > clazz )
	{
		return clazz.equals( String.class ) || ClassUtils.isPrimitiveOrWrapper( clazz );
	}

	public Map<String, List<String>> getSkipPropertyConfig()
	{
		return skipPropertyConfig;
	}

	public void setSkipPropertyConfig( Map<String, List<String>> skipPropertyConfig )
	{
		this.skipPropertyConfig = skipPropertyConfig;
	}

}
