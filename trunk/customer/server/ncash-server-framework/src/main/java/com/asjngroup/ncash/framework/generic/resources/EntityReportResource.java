package com.asjngroup.ncash.framework.generic.resources;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.asjngroup.ncash.framework.generic.models.EntityModel;
import com.asjngroup.ncash.framework.generic.services.EntityService;
import com.asjngroup.ncash.framework.generic.services.IEntityService;
import com.asjngroup.ncash.framework.security.helper.UserHelper;


public class EntityReportResource
{

	private IEntityService service = new EntityService();

	@GET
	@Path( "/{entityName}" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response genericReport( @PathParam( "entityName" ) String entityName, @QueryParam( "fields" ) String fields, @QueryParam( "aggrFun" ) String aggrFun, @Context UriInfo uriInfo, @Context MessageContext context )
	{
		List<Integer> partitions = UserHelper.getPartitionIdsForCurrentUser( context );

		String condition = uriInfo.getQueryParameters().getFirst( "filter" );
		Integer pageNum = null;
		Integer pageSize = null;
		Integer paccId = null;

		if ( uriInfo.getQueryParameters().containsKey( "pageNum" ) && uriInfo.getQueryParameters().containsKey( "pageSize" ) )
		{
			pageNum = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageNum" ) );
			pageSize = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "pageSize" ) );
		}
		if ( uriInfo.getQueryParameters().containsKey( "paccId" ) )
		{
			paccId = Integer.parseInt( uriInfo.getQueryParameters().getFirst( "paccId" ) );
		}
		List<EntityModel> models = service.queryEntities( entityName, fields, aggrFun, condition, partitions, pageNum, pageSize);

		return Response.ok( models ).build();
	}

}
