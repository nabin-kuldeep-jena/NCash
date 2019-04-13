package com.asjngroup.ncash.framework.generic.resources;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;
import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapper;
import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapperFactory;
import com.asjngroup.ncash.framework.generic.models.EntityModel;
import com.asjngroup.ncash.framework.generic.models.EntityRef;
import com.asjngroup.ncash.framework.generic.services.EntityService;
import com.asjngroup.ncash.framework.generic.services.IEntityService;
import com.asjngroup.ncash.framework.security.helper.UserHelper;

@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
public class GenericEntityResource
{
	// FIXME: It is recommended that this be autowired, but since the service is created by cxf, there is some problem with wiring
	private IEntityService service = new EntityService();
	public static final String PARTITION_ID = "partitionId";
	private CustomizeDataMapperFactory customizeDataMapperFactory;

	@GET
	@Path( "/{entityName}" )
	public Response getEntities( @PathParam( "entityName" ) String entityName, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		boolean isLinkRequired = false;
		isLinkRequired = Boolean.TRUE.equals( uriInfo.getQueryParameters().getFirst( "isLinkRequired" ) );
		String condition = uriInfo.getQueryParameters().getFirst( "filter" );
		Integer pageNum = null;
		Integer pageSize = null;

		if ( uriInfo.getQueryParameters().containsKey( "pageNum" ) && uriInfo.getQueryParameters().containsKey( "pageSize" ) )
		{
			pageNum = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageNum" ) );
			pageSize = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageSize" ) );
		}

		Session session = HibernateSession.openSession();
		List<EntityModel> models = new ArrayList<>();
		try
		{
			List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );
			CustomizeDataMapper customizeDataMapper = customizeDataMapperFactory.getCustomDataMapper( uriInfo.getQueryParameters().getFirst( "cmp" ) );
			boolean isLink = Boolean.valueOf( uriInfo.getQueryParameters().getFirst( "link" ) );
			models = service.getEntities( entityName, condition, partitions, pageNum, pageSize, session, customizeDataMapper,isLink );
			if ( isLinkRequired )
				updateLinks( models, getBaseUri( uriInfo.getBaseUri().toString() ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return ResponseBuilder.sendErrorResponse( "Internal Server Error", Status.INTERNAL_SERVER_ERROR, null );
		}
		finally
		{
			HibernateSession.closeSession( session );
		}

		return Response.ok( models ).build();
	}

	@GET
	@Path( "/{relationEntiy}/{entityName}" )
	public Response getEntitiesByRelation( @PathParam( "relationEntiy" ) String relationEntiy, @PathParam( "entityName" ) String entityName, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		String condition = uriInfo.getQueryParameters().getFirst( "filter" );
		Integer pageNum = null;
		Integer pageSize = null;

		if ( uriInfo.getQueryParameters().containsKey( "pageNum" ) && uriInfo.getQueryParameters().containsKey( "pageSize" ) )
		{
			pageNum = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageNum" ) );
			pageSize = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageSize" ) );
		}

		Session session = HibernateSession.openSession();
		List<EntityModel> models = new ArrayList<>();
		try
		{
			List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );
			boolean isLink = Boolean.valueOf( uriInfo.getQueryParameters().getFirst( "link" ) );
			models = service.getEntities( entityName, relationEntiy, condition, partitions, pageNum, pageSize, session ,isLink);
			updateLinks( models, getBaseUri( uriInfo.getBaseUri().toString() ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return ResponseBuilder.sendErrorResponse( "Internal Server Error", Status.INTERNAL_SERVER_ERROR, null );
		}
		finally
		{
			HibernateSession.closeSession( session );
		}

		return Response.ok( models ).build();
	}

	@GET
	@Path( "/{entityName}/{entityId}" )
	public Response getEntity( @PathParam( "entityName" ) String entityName, @PathParam( "entityId" ) String entityId, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		Session session = HibernateSession.openSession();
		List<EntityModel> models = new ArrayList<>();
		try
		{
			List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );
			CustomizeDataMapper customizeDataMapper = customizeDataMapperFactory.getCustomDataMapper( uriInfo.getQueryParameters().getFirst( "cmp" ) );
			boolean isLink = Boolean.valueOf( uriInfo.getQueryParameters().getFirst( "link" ) );
			models = service.getEntity( entityName, entityId, partitions, session, customizeDataMapper,isLink );
			updateLinks( models, getBaseUri( uriInfo.getBaseUri().toString() ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return ResponseBuilder.sendErrorResponse( "Internal Server Error", Status.INTERNAL_SERVER_ERROR, null );
		}
		finally
		{
			HibernateSession.closeSession( session );
		}

		return Response.ok( models ).build();
	}

	@POST
	@Path( "/{entityName}" )
	public Response createEntity( EntityModel model, @PathParam( "entityName" ) String entityName, @Context MessageContext context )
	{
		doPartitionChecks( model, context );

		EntityRef ref = service.createEntity( model );
		return Response.ok( ref ).build();
	}

	@PUT
	@Path( "/{entityName}/{entityId}" )
	public Response updateEntity( EntityModel model, @PathParam( "entityName" ) String entityName, @PathParam( "entityId" ) String entityId, @Context MessageContext context )
	{
		//FIXME: not sure if this is needed
		model.setId( entityId );

		doPartitionChecks( model, context );
		EntityRef ref = this.service.updateEntity( model );
		return Response.ok( ref ).build();
	}

	@PUT
	@Path( "/{entityName}" )
	public Response updateEntities( EntityModel model, @PathParam( "entityName" ) String entityName, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		doPartitionChecks( model, context );
		String condition = uriInfo.getQueryParameters().getFirst( "filter" );
		List<EntityRef> refs = this.service.updateEntities( model, condition );
		return Response.ok( refs ).build();
	}

	@DELETE
	@Path( "/{entityName}" )
	public Response deleteEntities( @PathParam( "entityName" ) String entityName, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );

		String condition = uriInfo.getQueryParameters().getFirst( "filter" );
		List<EntityRef> refs = this.service.deleteEntities( entityName, condition, partitions );
		return Response.ok( refs ).build();
	}

	@DELETE
	@Path( "/{entityName}/{entityId}" )
	public Response deleteEntity( @PathParam( "entityName" ) String entityName, @PathParam( "entityId" ) String entityId, @Context MessageContext context )
	{
		List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );
		List<EntityRef> refs = this.service.deleteEntity( entityName, entityId, partitions );
		return Response.ok( refs ).build();
	}

	private void doPartitionChecks( EntityModel model, MessageContext context )
	{
		List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );

		// partition checks
		Integer partition = null;
		if ( model.getProperties().containsKey( PARTITION_ID ) )
		{
			partition = Integer.parseInt( String.valueOf( model.getProperties().get( PARTITION_ID ) ) );
			if ( !partitions.contains( partition ) )
			{
				throw new RuntimeException( "Partition " + partition + " does not belong to user" );
			}
		}
		else
		{
			model.getProperties().put( PARTITION_ID, partitions.get( 0 ) );
		}

		/*if ( partitions.size() > 1 && partition == null )
		{
			//if the user has more than one partition and there is no partition provided in the data, then it is an error.
			throw new RuntimeException( "User has more than one partitions, provide a partition Id" );
		}*/
	}

	private void updateLinks( List<EntityModel> models, String baseUri )
	{
		for ( EntityModel entityModel : models )
		{
			entityModel.prependLink( baseUri );
		}
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public String getBaseUri( String uri )
	{
		return uri.substring( 0, uri.lastIndexOf( "/" ) );
	}

	public IEntityService getService()
	{
		return service;
	}

	public void setService( IEntityService service )
	{
		this.service = service;
	}

	public void setCustomizeDataMapperFactory( CustomizeDataMapperFactory customizeDataMapperFactory )
	{
		this.customizeDataMapperFactory = customizeDataMapperFactory;
	}

}
