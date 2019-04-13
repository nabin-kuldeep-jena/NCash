package com.asjngroup.ncash.framework.util;


import org.dozer.CustomFieldMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.dozer.util.ReflectionUtils;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;
import com.asjngroup.ncash.framework.generic.models.IsEntityModel;


public class CustomFieldMapperImpl implements CustomFieldMapper
{
	private Session session = null;

	private FieldMap lastFieldMapping;

	private Map<IsEntityModel, AbstractHibernateObject> mapModelToHibObject = new HashMap<IsEntityModel, AbstractHibernateObject>();

	private Map<AbstractHibernateObject, IsEntityModel> auditObjectMap = new HashMap<AbstractHibernateObject, IsEntityModel>();

	CustomFieldMapperImpl()
	{
	}

	CustomFieldMapperImpl( Session session )
	{
		this.session = session;
	}

	CustomFieldMapperImpl( boolean blockCascadeCopy )
	{
	}

	public boolean mapField( Object source, Object destination, Object sourceFieldValue, ClassMap classMap, FieldMap fieldMapping )
	{
		if ( sourceFieldValue == null )
		{
			return false;
		}

		setLastFieldMapping( classMap );

		if ( destination instanceof AbstractHibernateObject && source instanceof IsEntityModel && !auditObjectMap.containsKey( destination ) )
		{
			AbstractHibernateObject hObject = ( AbstractHibernateObject ) destination;
			IsEntityModel model = ( IsEntityModel ) source;
			auditObjectMap.put( hObject, model );
		}

		if ( fieldMapping.equals( lastFieldMapping ) )
		{
			Iterator<Entry<AbstractHibernateObject, IsEntityModel>> iterator = auditObjectMap.entrySet().iterator();
			while ( iterator.hasNext() )
			{
				Entry<AbstractHibernateObject, IsEntityModel> entry = iterator.next();
				AbstractHibernateObject ho = entry.getKey();
				IsEntityModel entityModel = entry.getValue();
				entityModel.setId( ho.getId() );
				/*if ( entityModel instanceof IsAuditableModel )
				{
					( ( IsAuditableModel ) entityModel ).setAuditingDisplayString( ho.getAuditingDisplayString() );
				}*/
				entityModel.setDisplayString( ho.getDisplayString() );
			}
		}

		if ( sourceFieldValue instanceof List && destination instanceof AbstractHibernateObject && source instanceof IsEntityModel )
		{
			if ( mapListToList( destination, sourceFieldValue, fieldMapping ) )
			{
				return true;
			}
		}

		if ( sourceFieldValue instanceof IsEntityModel )
		{
			if ( mapObject( destination, sourceFieldValue, fieldMapping ) )
			{
				return true;
			}
		}
		return false;
	}

	private void setLastFieldMapping( ClassMap classMap )
	{
		if ( lastFieldMapping == null && !classMap.getFieldMaps().isEmpty() )
		{
			lastFieldMapping = ( FieldMap ) classMap.getFieldMaps().get( classMap.getFieldMaps().size() - 1 );
		}
	}

	private boolean mapObject( Object destination, Object sourceFieldValue, FieldMap fieldMapping )
	{
		AbstractHibernateObject destObject = ( AbstractHibernateObject ) fieldMapping.getDestValue( destination );
		IsEntityModel model = ( IsEntityModel ) sourceFieldValue;
		Method readMethod = getGetMethod( destination, fieldMapping );
		if ( readMethod == null )
		{
			return false;
		}

		if ( isManyToOne( readMethod ) )
		{
			if ( setReferredObject( destination, fieldMapping, destObject, model ) )
			{
				return true;
			}
		}
		else if ( isOneToOne( readMethod ) )
		{
			if ( setOwnedObject( destination, fieldMapping, destObject, model ) )
			{
				return true;
			}
		}

		return false;
	}

	private boolean setReferredObject( Object destination, FieldMap fieldMapping, AbstractHibernateObject destObject, IsEntityModel model )
	{
		if ( !setObject( destination, fieldMapping, destObject, model ) )
		{
			return false;
		}
		return true;
	}

	private boolean setObject( Object destination, FieldMap fieldMapping, AbstractHibernateObject destObject, IsEntityModel model )
	{
		Class<AbstractHibernateObject> entityClazz = getDestinationClassType( destination, fieldMapping );
		if ( entityClazz == null )
		{
			return false;
		}

		AbstractHibernateObject entityObject = destObject;
		try
		{
			if ( destObject == null || destObject.getId() != model.getId() )
			{
				entityObject = createEntity( model, entityClazz );
				if ( entityObject == null )
				{
					return false;
				}
				Method setMethod = getSetMethod( destination, fieldMapping );
				if ( setMethod == null )
				{
					return false;
				}
				ReflectionUtils.invoke( setMethod, destination, new Object[]
				{ entityObject } );
			}
		}
		catch ( ObjectNotFoundException e )
		{
			entityObject = createEntity( model, entityClazz );
			if ( entityObject == null )
			{
				return false;
			}
			Method setMethod = getSetMethod( destination, fieldMapping );
			if ( setMethod == null )
			{
				return false;
			}
			ReflectionUtils.invoke( setMethod, destination, new Object[]
			{ entityObject } );
		}

		mapModelToHibObject.put( model, entityObject );
		return true;
	}

	private boolean setOwnedObject( Object destination, FieldMap fieldMapping, AbstractHibernateObject destObject, IsEntityModel model )
	{
		setObject( destination, fieldMapping, destObject, model );
		return false;
	}

	private AbstractHibernateObject createEntity( IsEntityModel model, Class<AbstractHibernateObject> entityClazz )
	{
		AbstractHibernateObject entityObject = mapModelToHibObject.get( model );
		try
		{
			if ( entityObject == null )
			{
				if ( session != null )
				{
					entityObject = ( AbstractHibernateObject ) session.get( entityClazz, Integer.valueOf( model.getId() ) );
				}

				if ( entityObject == null )
				{
					entityObject = entityClazz.newInstance();
					entityObject.setId( model.getId() );
				}
			}
		}
		catch ( InstantiationException e )
		{
			e.printStackTrace();
		}
		catch ( IllegalAccessException e )
		{
			e.printStackTrace();
		}
		return entityObject;
	}

	private boolean isManyToOne( Method readMethod )
	{
		ManyToOne manyToOne = readMethod.getAnnotation( ManyToOne.class );
		if ( manyToOne != null )
		{
			return true;
		}
		return false;
	}

	private boolean isOneToOne( Method readMethod )
	{
		OneToOne oneToOne = readMethod.getAnnotation( OneToOne.class );
		if ( oneToOne != null )
		{
			return true;
		}
		return false;
	}

	private Method getGetMethod( Object destination, FieldMap fieldMapping )
	{
		Method method = ReflectionUtils.findPropertyDescriptor( destination.getClass(), fieldMapping.getDestFieldName(), null ).getReadMethod();
		return method;
	}

	@SuppressWarnings( "unchecked" )
	private boolean mapListToList( Object destination, Object sourceFieldValue, FieldMap fieldMapping )
	{

		List<IsEntityModel> sourceModels = ( List<IsEntityModel> ) sourceFieldValue;
		List<AbstractHibernateObject> destObjects = ( List<AbstractHibernateObject> ) fieldMapping.getDestValue( destination );
		Class<AbstractHibernateObject> genericType = getGenericClassType( destination, fieldMapping );
		Method addMethod = getAddMethod( destination, fieldMapping );
		if ( addMethod != null )
		{
			for ( IsEntityModel model : sourceModels )
			{
				AbstractHibernateObject hibObject = getHibernateObject( model, destObjects );
				if ( hibObject != null )
				{
					update( model, hibObject );
				}
				else
				{
					add( destination, genericType, addMethod, model );
				}
			}
			return true;
		}
		return false;
	}

	private <H extends AbstractHibernateObject> void add( Object destination, Class<H> genericType, Method addMethod, IsEntityModel model )
	{
		AbstractHibernateObject hibObject = mapModelToHibObject.get( model );
		if ( hibObject == null )
		{
			hibObject = DozerServiceUtil.dto2Hibernate( genericType, model, this );
			mapModelToHibObject.put( model, hibObject );
		}
		else
		{
			DozerServiceUtil.dto2Hibernate( hibObject, model, this );
		}
		ReflectionUtils.invoke( addMethod, destination, new Object[]
		{ hibObject } );
	}

	private void update( IsEntityModel model, AbstractHibernateObject hibObject )
	{
		DozerServiceUtil.dto2Hibernate( hibObject, model, this );
	}

	@SuppressWarnings( "unchecked" )
	private <H extends AbstractHibernateObject> Class<H> getGenericClassType( Object destination, FieldMap fieldMapping )
	{
		Method method = getSetMethod( destination, fieldMapping );
		Class<H> genericType = ( Class<H> ) ReflectionUtils.determineGenericsType( method, false );
		return genericType;
	}

	@SuppressWarnings( "unchecked" )
	private <H extends AbstractHibernateObject> Class<H> getDestinationClassType( Object destination, FieldMap fieldMapping )
	{
		Method method = getSetMethod( destination, fieldMapping );
		
		//need to verify
		Object[] parameterTypes = ( Object[] ) ReflectionUtils.invoke(method, method, null );

		if ( parameterTypes != null && parameterTypes.length > 0 )
		{
			Class<H> clazzType = ( Class<H> ) parameterTypes[0];
			return clazzType;
		}
		return null;
	}

	private Method getSetMethod( Object destination, FieldMap fieldMapping )
	{
		Method method = ReflectionUtils.findPropertyDescriptor( destination.getClass(), fieldMapping.getDestFieldName(), null ).getWriteMethod();
		return method;
	}

	@SuppressWarnings( "unchecked" )
	private <H extends AbstractHibernateObject> Method getAddMethod( Object destination, FieldMap fieldMapping )
	{
		Method method = getSetMethod( destination, fieldMapping );
		if ( method == null )
		{
			return null;
		}
		String addMethodName = javaObjectBuilderLogic( method );
		Class<H> parent = ( Class<H> ) destination.getClass();
		Method addMethod = getMethod( addMethodName, parent );
		return addMethod;
	}

	private String javaObjectBuilderLogic( Method method )
	{
		String addMethodName = "add" + method.getName().substring( 3 );
		if ( addMethodName.endsWith( "es" ) )
		{
			addMethodName = addMethodName.substring( 0, addMethodName.length() - 2 );
		}
		else if ( addMethodName.endsWith( "s" ) )
		{
			addMethodName = addMethodName.substring( 0, addMethodName.length() - 1 );
		}
		return addMethodName;
	}

	private AbstractHibernateObject getHibernateObject( IsEntityModel model, List<AbstractHibernateObject> destObjects )
	{
		for ( AbstractHibernateObject hibObject : destObjects )
		{
			if ( model.getId() == hibObject.getId() )
			{
				return hibObject;
			}
		}
		return null;
	}

	private <H extends AbstractHibernateObject> Method getMethod( String addMethodName, Class<H> parent )
	{
		for ( Method item : parent.getDeclaredMethods() )
		{
			if ( item.getName().equalsIgnoreCase( addMethodName ) )
			{
				return item;
			}
		}
		return null;
	}
}