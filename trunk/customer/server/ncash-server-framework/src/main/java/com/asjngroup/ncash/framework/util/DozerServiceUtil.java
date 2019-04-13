package com.asjngroup.ncash.framework.util;

import org.dozer.CustomFieldMapper;
import org.hibernate.Session;

import java.util.Collection;
import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;
import com.asjngroup.ncash.framework.generic.models.IsEntityModel;



@SuppressWarnings( "unchecked" )
public class DozerServiceUtil
{
	public static <T> List<T> hibernateList2DTOList( Class<T> destClass, List< ? extends AbstractHibernateObject> hList )
	{
		List<T> dtoList = null;
		if ( hList != null )
		{
			dtoList = DozerService.getService().toDTOObjects( hList, destClass );
		}
		return dtoList;
	}

	public static <D> D hibernate2DTO( Class<D> destinationClass, AbstractHibernateObject hibObj )
	{
		if ( hibObj != null )
		{
			return ( D ) DozerService.getService().toDTOObject( hibObj, destinationClass );
		}
		return null;
	}

	public static <D> D hibernate2DTO( Class<D> destinationClass, AbstractHibernateObject hibObj, boolean customInternationaliser )
	{
		if ( hibObj != null )
		{
			return ( D ) DozerService.getService().toDTOObject( hibObj, destinationClass, customInternationaliser );
		}
		return null;
	}

	public static <T> List<T> hibernateList2DTOList( Class<T> destClass, Collection< ? extends AbstractHibernateObject> hList )
	{
		List<T> dtoList = null;
		if ( hList != null )
		{
			dtoList = DozerService.getService().toDTOObjects( hList, destClass );
		}
		return dtoList;
	}

	public static <H> H dto2Hibernate( Class<H> destinationClass, IsEntityModel dtoObj )
	{
		if ( dtoObj != null )
		{
			return ( H ) DozerService.getService().toHibernateObject( dtoObj, destinationClass );
		}
		return null;
	}

	public static <H> void dto2Hibernate( AbstractHibernateObject destination, IsEntityModel dtoObj )
	{
		if ( dtoObj != null )
		{
			DozerService.getService().toHibernateObject( dtoObj, destination );
		}
	}

	public static <H> H dto2Hibernate( Class<H> destinationClass, IsEntityModel dtoObj, boolean customWrapper )
	{
		if ( dtoObj != null )
		{
			return ( H ) DozerService.getService().toHibernateObject( dtoObj, destinationClass, customWrapper );
		}
		return null;
	}

	public static <H> void dto2Hibernate( AbstractHibernateObject destination, IsEntityModel dtoObj, boolean customWrapper )
	{
		if ( dtoObj != null )
		{
			DozerService.getService().toHibernateObject( dtoObj, destination, customWrapper );
		}
	}

	public static <H> H dto2Hibernate( Class<H> destinationClass, IsEntityModel dtoObj, CustomFieldMapper customWrapper )
	{
		if ( dtoObj != null )
		{
			return ( H ) DozerService.getService().toHibernateObject( dtoObj, destinationClass, customWrapper );
		}
		return null;
	}

	public static <H> void dto2Hibernate( AbstractHibernateObject destination, IsEntityModel dtoObj, CustomFieldMapper customWrapper )
	{
		if ( dtoObj != null )
		{
			DozerService.getService().toHibernateObject( dtoObj, destination, customWrapper );
		}
	}

	public static <H> H dto2Hibernate( Class<H> destinationClass, IsEntityModel dtoObj, boolean customWrapper, Session session )
	{
		if ( dtoObj != null )
		{
			return ( H ) DozerService.getService().toHibernateObject( dtoObj, destinationClass, customWrapper, session );
		}
		return null;
	}

	public static <H> void dto2Hibernate( AbstractHibernateObject destination, IsEntityModel dtoObj, boolean customWrapper, Session session )
	{
		if ( dtoObj != null )
		{
			DozerService.getService().toHibernateObject( dtoObj, destination, customWrapper, session );
		}
	}

	public static <H> H dto2Hibernate( Class<H> destinationClass, IsEntityModel dtoObj, boolean customWrapper, boolean blockCascade )
	{
		if ( dtoObj != null )
		{
			return ( H ) DozerService.getService().toHibernateObject( dtoObj, destinationClass, customWrapper, blockCascade );
		}
		return null;
	}

	public static <H> void dto2Hibernate( AbstractHibernateObject destination, IsEntityModel dtoObj, boolean customWrapper, boolean blockCascade )
	{
		if ( dtoObj != null )
		{
			DozerService.getService().toHibernateObject( dtoObj, destination, customWrapper, blockCascade );
		}
	}

	public static <D> List<D> dtoList2HibernateList( Class<D> destClass, List< ? extends IsEntityModel> dtoList )
	{
		List<D> hList = null;
		if ( dtoList != null )
		{
			hList = DozerService.getService().toHibernateObjects( dtoList, destClass );
		}
		return hList;
	}
}
