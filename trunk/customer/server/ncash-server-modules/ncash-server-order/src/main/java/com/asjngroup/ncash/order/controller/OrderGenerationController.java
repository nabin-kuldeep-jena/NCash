package com.asjngroup.ncash.order.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;
import com.asjngroup.ncash.order.service.models.OrderCreationResponseModel;
import com.asjngroup.ncash.order.service.models.OrderTblModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Component( value = "ordergenerationController" )
@Api( value = "/order", consumes = "application/json", tags =
{ "Order" }, produces = "application/json", description = "This api used to create and manage order." )
public class OrderGenerationController
{
	@Autowired( required = true )
	OrderGenerationService orderGenerationService;

	@POST
	@Path( NCashServicesConstant.CREATE_NEW_ORDER )
	@ApiOperation( value = "post operation to create new order", response = OrderCreationResponseModel.class )
	public Response createNewOrder( @ApiParam( value = "order model", required = true ) OrderTblModel orderTblModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( 1 );
		OrderCreationResponseModel orderCreationResponseModel = orderGenerationService.createNewOrder( orderTblModel, context, responseMessageModel );
		if(orderCreationResponseModel==null)
			return Response.status( Status.NOT_ACCEPTABLE ).entity( responseMessageModel ).build();
		else
			return Response.status( Status.ACCEPTED ).entity( orderCreationResponseModel ).build();
	}
	
	public void setOrderGenerationService( OrderGenerationService orderGenerationService )
	{
		this.orderGenerationService = orderGenerationService;
	}

}
