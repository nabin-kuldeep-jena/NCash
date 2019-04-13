
package com.asjngroup.ncash.framework.generic.services;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.BasicTransformerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapper;
import com.asjngroup.ncash.framework.generic.models.EntityModel;
import com.asjngroup.ncash.framework.generic.models.EntityRef;
import com.asjngroup.ncash.framework.generic.util.EntityRelationModel;
import com.asjngroup.ncash.framework.generic.util.EntityRelationUtil;

public class EntityService implements IEntityService
{
	private EntityMapper mapper = new EntityMapper();
	final String PUT = "PUT";

	/*public EntityService()
	{
		this.mapper = new EntityMapper();
		this.mapper.setSkipPropertyConfig( new HashMap<String, List<String>>()
		{
	
			{
				put( "*", new ArrayList<String>()
				{
	
					{
						add( "class" );
					}
				} );
			}
		} );
	}
	*/
	@Override
	public List<EntityModel> getEntity( String entityName, String entityId, List<Integer> partitions, Session session, CustomizeDataMapper customizeDataMapper,boolean isLinkReq )
	{
		return getEntities( entityName, "id=" + entityId, partitions, null, null, session, customizeDataMapper,isLinkReq );
	}

	@Override
	public List<EntityModel> getEntities( String entityName, String condition, List<Integer> partitions, Integer pageNum, Integer pageSize, Session session, CustomizeDataMapper customizeDataMapper,boolean isLinkReq )
	{
		List<EntityModel> models = new ArrayList<>();

		try
		{
			List resultObjs = queryEntities( session, entityName, condition, null, partitions, pageNum, pageSize );
			for ( Object obj : resultObjs )
			{
				Set<String> nestedObjects=new HashSet<>();
				if ( customizeDataMapper != null )
					models.add( mapper.map( obj, customizeDataMapper,nestedObjects,isLinkReq ) );
				else
					models.add( mapper.map( obj,nestedObjects,isLinkReq ) );
			}

		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new NCashRuntimeException( "Model conversion failed", e );
		}

		return models;
	}

	@Override
	public List<EntityModel> getEntities( String entityName, String relationalEntity, String condition, List<Integer> partitions, Integer pageNum, Integer pageSize,Session session,boolean isLinkReq )
	{
		List<EntityModel> models = new ArrayList<>();

		try
		{
			EntityRelationModel entityRelationModel = EntityRelationUtil.getEntityRealtionByKey( relationalEntity, entityName );
			List resultObjs = queryEntities( session, entityName, condition, entityRelationModel, partitions, pageNum, pageSize );
			for ( Object obj : resultObjs )
			{
				Set<String> nestedObjects=new HashSet<>();
				models.add( mapper.map( obj,nestedObjects,isLinkReq ) );
			}

		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Model conversion failed", e );
		}

		return models;
	}

	private List queryEntities( Session session, String entityName, String condition, EntityRelationModel entityRelationModel, List<Integer> partitions, Integer pageNum, Integer pageSize )
	{
		StringBuilder sb = new StringBuilder( "from " );
		sb.append( entityName );

		if ( entityRelationModel != null )
		{
			if ( condition != null )
				condition += " and " + entityRelationModel.getRelationShip();
			else
				condition = entityRelationModel.getRelationShip();
		}

		boolean isPartitionTbl = "PartitionTbl".equals( entityName );
		if ( condition != null || !isPartitionTbl )
			sb.append( " where " );
		if ( !isPartitionTbl )
		{
			sb.append( " partitionId in ( " );
			for ( int i = 0; i < partitions.size(); i++ )
			{
				if ( i > 0 )
				{
					sb.append( "," );
				}
				sb.append( partitions.get( i ) );
			}
			sb.append( " )" );
		}

		if ( condition != null )
		{
			if ( !isPartitionTbl )
				sb.append( " and " );
			sb.append( condition );
		}

		Query query = session.createQuery( sb.toString() );
		if ( pageNum != null && pageSize != null )
		{
			query.setFirstResult( pageNum );
			query.setMaxResults( pageSize );
		}

		return query.list();
	}

	@Override
	public EntityRef createEntity( EntityModel entityModel )
	{
		Session session = HibernateSession.openSession();
		EntityRef ref = null;
		Transaction txn = null;
		try
		{
			txn = session.beginTransaction();
			Object object = this.mapper.map( entityModel, session );
			session.save( object );
			txn.commit();
			ref = this.mapper.mapRef( object );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			if ( txn != null )
			{
				txn.rollback();
			}
			//FIXME: find a proper exception to throw
			throw new RuntimeException( "Failed to persist entity", e );
		}
		finally
		{
			HibernateSession.closeSession( session );
		}

		return ref;
	}

	@Override
	public EntityRef updateEntity( EntityModel entityModel )
	{
		EntityRef ref = null;
		List<EntityRef> refs = updateEntities( entityModel, "id=" + entityModel.getId() );
		if ( !refs.isEmpty() )
		{
			ref = refs.get( 0 );
		}
		return ref;
	}

	@Override
	public List<EntityRef> updateEntities( EntityModel entityModel, String condition )
	{
		List<EntityRef> refs = new ArrayList<>();
		Session session = HibernateSession.openSession();
		Transaction txn = null;
		try
		{
			txn = session.beginTransaction();

			Integer partition = Integer.parseInt( String.valueOf( entityModel.getProperties().get( "partitionId" ) ) );
			List resultObjs = queryEntities( session, entityModel.getEntityName(), condition, null, Arrays.asList( partition ), null, null );

			for ( Object object : resultObjs )
			{
				this.mapper.mapProperties( object, entityModel, session, PUT );
				session.update( object );
				refs.add( this.mapper.mapRef( object ) );
			}

			txn.commit();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			if ( txn != null )
			{
				txn.rollback();
			}
			//FIXME: find a proper exception to throw
			throw new RuntimeException( "Failed to update entity", e );
		}
		finally
		{
			HibernateSession.closeSession( session );
		}
		return refs;
	}

	@Override
	public List<EntityRef> deleteEntity( String entityName, String entityId, List<Integer> partitions )
	{
		return deleteEntities( entityName, "id=" + entityId, partitions );
	}

	@Override
	public List<EntityRef> deleteEntities( String entityName, String condition, List<Integer> partitions )
	{
		List<EntityRef> refs = new ArrayList<>();
		Session session = HibernateSession.openSession();
		Transaction txn = null;
		try
		{
			txn = session.beginTransaction();
			List objects = queryEntities( session, entityName, condition, null, partitions, null, null );
			for ( Object object : objects )
			{
				refs.add( this.mapper.mapRef( object ) );
				session.delete( object );
			}
			txn.commit();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			txn.rollback();
		}
		finally
		{
			HibernateSession.closeSession( session );
		}
		return refs;
	}

	public EntityMapper getMapper()
	{
		return mapper;
	}

	public void setMapper( EntityMapper mapper )
	{
		this.mapper = mapper;
	}

	public Query createQuery( Session session, String entityName, String condition, String aggrFun, List<Integer> partitions, Integer pageNum, Integer pageSize, String tablePrefix )
	{
		return createQuery( session, entityName, null, condition, aggrFun, partitions, pageNum, pageSize, tablePrefix );
	}

	public Query createQuery( Session session, String entityName, String fields, String aggrFun, String condition, List<Integer> partitions, Integer pageNum, Integer pageSize, String tablePrefix )
	{

		StringBuilder sb = new StringBuilder();

		if ( fields != null )
		{
			sb.append( "select " );
			/* aggrFun will work only for first parameter */
			if ( aggrFun == null )
				sb.append( tablePrefix ).append( "Id, " );
			else
				sb.append( aggrFun ).append( " " );
			sb.append( fields );
		}

		sb.append( " from " ).append( entityName ).append( " where partitionId in ( " );
		for ( int i = 0; i < partitions.size(); i++ )
		{
			if ( i > 0 )
			{
				sb.append( "," );
			}
			sb.append( partitions.get( i ) );
		}
		sb.append( " )" );

		if ( condition != null )
		{
			sb.append( " and " ).append( condition );
		}

		Query query = session.createQuery( sb.toString() );
		if ( pageNum != null && pageSize != null )
		{
			query.setFirstResult( pageNum );
			query.setMaxResults( pageSize );
		}
		return query;
	}

	@Override
	public List<EntityModel> queryEntities( String entityName, String fields, String aggrFun, String condition, List<Integer> partitions, Integer pageNum, Integer pageSize )
	{
		List<EntityModel> models = new ArrayList<>();

		Session session = HibernateSession.openSession();

		Object prefix = session.createQuery( "select tbdPrefix from TableDfn where tbdName = '" + StringHelper.camelCaseToUnderScore( entityName ) + "'" ).list().get( 0 );
		String tablePrefix = prefix.toString();

		/*Query query =session.createQuery( "from PaymentTbl where partitionId in ( 1 ) and pmtDate='2016/jan/01 00:00:34'" ); 
		 query.list();*/

		Query query = createQuery( session, entityName, fields, aggrFun, condition, partitions, pageNum, pageSize, tablePrefix );
		try
		{
			//Map<String, ClassMetadata> metaDataMap = new HashMap<>();
			MapTransformer transformer = new MapTransformer();
			transformer.setHeader( getHeaders( tablePrefix, fields, aggrFun ) );
			query.setResultTransformer( transformer );

			List<Map<String, Object>> list = query.list();
			for ( Map<String, Object> row : list )
			{
				EntityModel model = new EntityModel();
				model.setEntityName( "Report" );
				if ( aggrFun == null )
					model.setId( row.get( tablePrefix + "Id" ).toString() );
				model.setProperties( row );
				models.add( model );
			}

		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		return models;
	}

	private List<String> getHeaders( String tablePrefix, String fields, String aggrFun )
	{
		List<String> headers = new ArrayList<>();

		/* Columns Should be Ordered */
		if ( aggrFun == null )
			headers.add( tablePrefix + "Id" );

		String[] split = fields.split( "," );
		for ( String string : split )
		{
			headers.add( string.trim() );
		}
		return headers;
	}

	@SuppressWarnings( "serial" )
	private static class MapTransformer extends BasicTransformerAdapter
	{

		private List<String> header;

		/**
		 * {@inheritDoc}
		 */
		public Object transformTuple( Object[] tuple, String[] aliases )
		{
			Map result = new HashMap( tuple.length );
			for ( int i = 0; i < tuple.length; i++ )
			{
				String key = header.get( i );
				if ( key != null )
				{
					result.put( key, tuple[i] );
				}
			}
			return result;
		}

		public List<String> getHeader()
		{
			return header;
		}

		public void setHeader( List<String> header )
		{
			this.header = header;
		}

	}
}
