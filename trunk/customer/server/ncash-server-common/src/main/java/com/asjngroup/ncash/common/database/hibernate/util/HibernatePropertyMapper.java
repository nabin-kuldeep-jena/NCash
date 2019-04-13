package com.asjngroup.ncash.common.database.hibernate.util;

import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import java.util.*;

import com.asjngroup.ncash.common.database.hibernate.exception.HibernateUtilException;
import com.asjngroup.ncash.common.database.schema.EntityMapping;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.util.ObjectHelper;
import com.asjngroup.ncash.common.util.StringHelper;

/**
 * User: nabin.jena
 *
 * a util for mapping properties to id and vice versa. for example used by the installer when
 * deploying seed data and converting things like Name to the correct Id and reference xml
 * has no ids
 *
 * to add a new mapping simply add an entry to the static constructor.
 */
public class HibernatePropertyMapper
{
	private Map<String, PropertyMappingInfo> idToPropertyMappings = new HashMap<String, PropertyMappingInfo>();
	private Map<List<String>, PropertyMappingInfo> propertyToIdMappings = new HashMap<List<String>, PropertyMappingInfo>();
	private Map<Class, PropertyMappingInfo> classMappings = new HashMap<Class, PropertyMappingInfo>();
	private List<PropertyMappingInfo> mappingInfos = new ArrayList<PropertyMappingInfo>();

	private Map<Class, Map<Integer, List<Object>>> propertyLookupCache = new HashMap<Class, Map<Integer, List<Object>>>();
	private Map<Class, Map<List<Object>, Integer>> idLookupCache = new HashMap<Class, Map<List<Object>, Integer>>();

	private List<Class> dirtyList = new ArrayList<Class>();
	private SessionFactory sessionFactory;
	private boolean partialLoad = false;

	class PropertyMappingInfo
	{
		String idPropertyName;
		String convertPropertyNames[];
		Class clazz;

		public PropertyMappingInfo( String idPropertyName, String[] convertPropertyNames, Class clazz )
		{
			if ( idPropertyName == null )
				throw new NullPointerException( "id property is null" );

			if ( convertPropertyNames == null )
				throw new NullPointerException( "converted-property-names is null" );

			if ( clazz == null )
				throw new NullPointerException( "null Class definition" );

			this.idPropertyName = idPropertyName;
			this.convertPropertyNames = convertPropertyNames;

			// sort the property key into order
			Arrays.sort( this.convertPropertyNames );
			this.clazz = clazz;

		}

		public PropertyMappingInfo( String idPropertyName, String convertPropertyName, Class clazz )
		{
			this( idPropertyName, new String[]
			{ convertPropertyName }, clazz );
		}
	}

	public HibernatePropertyMapper( SessionFactory sessionFactory )
	{
		this( sessionFactory, false );
	}

	public HibernatePropertyMapper( SessionFactory sessionFactory, boolean partial )
	{
		this.sessionFactory = sessionFactory;
		this.partialLoad = partial;
	}

	public void initPropertyMappings( Schema schema ) throws HibernateException
	{
		// build a map from class name to interface class
		Map<String, Class> classInterfaceMap;
		try
		{
			classInterfaceMap = HibernateConfigurationHelper.buildClassNameInterfaceMap( HibernateSession.getConfiguration() );
		}
		catch ( HibernateUtilException e )
		{
			throw new HibernateException( e );
		}

		for ( EntityMapping entityMapping : schema.EntityMappings )
		{
			if ( entityMapping.getBusinessConstraints().length == 0 )
				continue;

			if ( partialLoad && classInterfaceMap.get( entityMapping.EntityName ) == null )
				continue;

			try
			{
				mappingInfos.add( new PropertyMappingInfo( entityMapping.PrimaryKey, entityMapping.getBusinessConstraints(), classInterfaceMap.get( entityMapping.EntityName ) ) );
			}
			catch ( NullPointerException e )
			{
				throw new NullPointerException( "Null Error processing in object-mapping '" + entityMapping.EntityName + "'" );
			}
		}

		for ( PropertyMappingInfo mappingInfo : mappingInfos )
		{
			idToPropertyMappings.put( mappingInfo.idPropertyName, mappingInfo );
			propertyToIdMappings.put( Arrays.asList( mappingInfo.convertPropertyNames ), mappingInfo );
			classMappings.put( mappingInfo.clazz, mappingInfo );

			markDirty( mappingInfo.clazz );
		}
	}

	public void markDirty( Class clazz )
	{
		if ( dirtyList.contains( clazz ) )
			return;

		dirtyList.add( clazz );
	}

	public boolean isDirty( Class clazz )
	{
		return dirtyList.contains( clazz );
	}

	private void dirtyCheck( Class clazz ) throws HibernateException
	{
		if ( isDirty( clazz ) )
		{
			reset( clazz );
		}
	}

	public void clearDirty( Class clazz )
	{
		if ( !dirtyList.contains( clazz ) )
			return;

		dirtyList.remove( clazz );
	}

	public void reset( SessionFactory sessionFactory ) throws HibernateException
	{
		for ( PropertyMappingInfo mappingInfo : mappingInfos )
		{
			reset( sessionFactory, mappingInfo.clazz );
		}
	}

	public void reset() throws HibernateException
	{
		for ( PropertyMappingInfo mappingInfo : mappingInfos )
		{
			reset( sessionFactory, mappingInfo.clazz );
		}
	}

	public void reset( Class clazz ) throws HibernateException
	{
		reset( sessionFactory, clazz );
	}

	public void reset( SessionFactory sessionFactory, Class clazz ) throws HibernateException
	{
		Session session = sessionFactory.openSession();
		if ( !classMappings.containsKey( clazz ) )
		{
			return;
		}

		PropertyMappingInfo mappingInfo = classMappings.get( clazz );
		try
		{
			List<Object> results = HibernateUtil.query( session, "from " + mappingInfo.clazz.getName() );
			ClassMetadata metadata = HibernateUtil.getClassMetadata( sessionFactory, mappingInfo.clazz );
			Map<Integer, List<Object>> idCache = new HashMap<Integer, List<Object>>();
			Map<List<Object>, Integer> propertyCache = new HashMap<List<Object>, Integer>();
			propertyLookupCache.put( mappingInfo.clazz, idCache );
			idLookupCache.put( mappingInfo.clazz, propertyCache );
			for ( Object obj : results )
			{
				Integer id = ( Integer ) metadata.getIdentifier( obj );

				List<Object> propertyValues = new ArrayList<Object>();
				;

				for ( int i = 0; i < mappingInfo.convertPropertyNames.length; i++ )
				{
					Object propertyValue = metadata.getPropertyValue( obj, mappingInfo.convertPropertyNames[i] );
					propertyValues.add( propertyValue );
				}
				propertyCache.put( propertyValues, id );
				idCache.put( id, propertyValues );
			}
		}
		finally
		{
			session.close();
		}

		clearDirty( clazz );
	}

	private PropertyMappingInfo findIdPropertyMappingInfo( String propertyName )
	{
		return idToPropertyMappings.get( propertyName );
	}

	private PropertyMappingInfo findPropertyMappingInfo( String propertyName )
	{
		return findPropertyMappingInfo( Arrays.asList( propertyName ) );
	}

	// assumes propertyNames has been sorted into alphabetical order ( Arrays.sort( propertyNames ) )
	private PropertyMappingInfo findPropertyMappingInfo( List<String> propertyNames )
	{
		return propertyToIdMappings.get( propertyNames );
	}

	// returns a map of entity names to business search keys, eg ( ScreenTbl -> { ScrName, AppId } )
	public Map<String, List<String>> getEntitySearchKeyMap()
	{
		Map<String, List<String>> entitySearchKeyMap = new HashMap<String, List<String>>();

		for ( PropertyMappingInfo propertyMappingInfo : mappingInfos )
		{
			entitySearchKeyMap.put( ObjectHelper.getClassOnlyName( propertyMappingInfo.clazz ), Arrays.asList( propertyMappingInfo.convertPropertyNames ) );
		}

		return entitySearchKeyMap;
	}

	// returns a map of entity names to FLATTENED business search keys,
	// eg ( Component -> { CmpName, CptTypeCd } )
	// CptId has been recursively flattened to CptTypeCd
	public Map<String, List<String>> getFlattenedEntitySearchKeyMap()
	{
		Map<String, List<String>> entitySearchKeyMap = new HashMap<String, List<String>>();

		for ( PropertyMappingInfo propertyMappingInfo : mappingInfos )
		{
			entitySearchKeyMap.put( ObjectHelper.getClassOnlyName( propertyMappingInfo.clazz ), flattenKeys( Arrays.asList( propertyMappingInfo.convertPropertyNames ) ) );
		}

		return entitySearchKeyMap;
	}

	private List<String> flattenKeys( List<String> keys )
	{
		List<String> flattenedKeys = new ArrayList<String>();

		for ( String key : keys )
		{
			flattenedKeys.addAll( flattenKey( key ) );
		}

		return flattenedKeys;
	}

	// flatten a key down to it's root business constraints, if the key is not an id it
	// will return a single element list containing the input parameter
	// eg CmpId flattens to CmpName, CptId and flattening recursively gives CmpName, CptTypeCd.
	private List<String> flattenKey( String key )
	{
		List<String> flattenedKeys = new ArrayList<String>();
		if ( idToPropertyMappings.containsKey( key ) )
		{
			PropertyMappingInfo propertyMappingInfo = idToPropertyMappings.get( key );

			for ( String subKey : propertyMappingInfo.convertPropertyNames )
			{
				// check for infinite recursive loops
				if ( subKey.equals( key ) )
					continue;

				flattenedKeys.addAll( flattenKey( subKey ) );
			}
		}
		else
		{
			flattenedKeys.add( key );
		}

		return flattenedKeys;
	}

	public boolean mapPropertyToId( Object obj, ClassMetadata metadata, Map<String, String> properties, String propertyName, Object propertyValue ) throws HibernateException
	{
		// get the suffix of this property ( xxxCmpName -> CmpName )
		// have to scan to two and three upper cases for things like CptTypeCd
		String suffix = StringHelper.getCamelCaseSuffix( propertyName, 2 );

		PropertyMappingInfo propertyMappingInfo = findPropertyMappingInfo( suffix );
		if ( propertyMappingInfo == null )
		{
			suffix = StringHelper.getCamelCaseSuffix( propertyName, 3 );

			propertyMappingInfo = findPropertyMappingInfo( suffix );
		}

		if ( propertyMappingInfo == null )
			return false;

		// don't do a mapping if this property actually belongs to this object ( CmpName -> Component )
		// AND if is isn't a suffix, it might be a self refering key with a prefix.
		if ( propertyMappingInfo.clazz.isAssignableFrom( obj.getClass() ) && propertyName.length() == suffix.length() )
			return false;

		// check for dirti-ness of this class, reset if it is
		dirtyCheck( propertyMappingInfo.clazz );

		Integer id = idLookupCache.get( propertyMappingInfo.clazz ).get( Arrays.asList( propertyValue ) );

		String idPropertyName = propertyName.substring( 0, propertyName.length() - suffix.length() ) + propertyMappingInfo.idPropertyName;
		metadata.setPropertyValue( obj, idPropertyName, id );

		return true;
	}

	public boolean mapIdToAttribute( Element element, Object obj, ClassMetadata metadata, String propertyName ) throws HibernateException
	{
		// get the suffix of this property ( xxxCmpId -> CmpId )
		// only need to scan for two upper cases as always looking for XxxId
		String suffix = StringHelper.getCamelCaseSuffix( propertyName, 2 );

		if ( idToPropertyMappings.containsKey( suffix ) )
		{
			PropertyMappingInfo propertyMappingInfo = idToPropertyMappings.get( suffix );

			dirtyCheck( propertyMappingInfo.clazz );

			Object propertyValue = metadata.getPropertyValue( obj, propertyName );
			List<Object> propertyObjects = propertyLookupCache.get( propertyMappingInfo.clazz ).get( propertyValue );

			for ( int i = 0; i < propertyObjects.size(); i++ )
			{
				String suffixedPropertyName = StringHelper.getCamelCaseSuffix( propertyName, 2 ) + propertyMappingInfo.convertPropertyNames[i];

				if ( propertyObjects.get( i ) == null )
				{
					element.addAttribute( suffixedPropertyName, "" );
				}
				else
				{
					element.addAttribute( suffixedPropertyName, propertyObjects.get( i ).toString() );
				}
			}
			return true;
		}

		return false;
	}

	public Integer attributeToId( String attributeName, Object attributeValue ) throws HibernateException
	{
		PropertyMappingInfo propertyMappingInfo = findPropertyMappingInfo( attributeName );

		if ( propertyMappingInfo == null )
			return null;

		dirtyCheck( propertyMappingInfo.clazz );

		return idLookupCache.get( propertyMappingInfo.clazz ).get( Arrays.asList( attributeValue ) );
	}

	public Object idToAttribute( String attributeName, Integer attributeValue ) throws HibernateException
	{
		if ( idToPropertyMappings.containsKey( attributeName ) )
		{
			PropertyMappingInfo propertyMappingInfo = idToPropertyMappings.get( attributeName );

			dirtyCheck( propertyMappingInfo.clazz );

			return propertyLookupCache.get( propertyMappingInfo.clazz ).get( attributeValue );
		}

		return null;
	}
}
