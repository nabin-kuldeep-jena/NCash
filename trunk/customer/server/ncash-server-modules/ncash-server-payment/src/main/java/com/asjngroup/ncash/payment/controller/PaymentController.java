package com.asjngroup.ncash.payment.controller;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Component( value = "paymentController" )
@Api( value = "/payment", consumes = "application/json", tags =
{ "Payment" }, produces = "application/json", description = "This api used to verify and manage payment transaction." )
public class PaymentController
{
	@Autowired( required = true )
	PaymentService paymentService;

	@POST
	@Path( NCashServicesConstant.VERIFY_PAYMENT_BY_ID )
	@ApiOperation( value = "post operation to verify payment", response = UserTbl.class )
	public Response verifyPaymentById( @ApiParam( value = "payment Id", required = true ) String gatewayPaymentId,@ApiParam( value = "paid amount", required = true )Double paidAmount,@ApiParam( value = "payment Id", required = true ) Long orderId, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( 1 );
		paymentService.verifyPayment( gatewayPaymentId,paidAmount,orderId, context, responseMessageModel );
		if(responseMessageModel==null)
			return Response.status( Status.NOT_ACCEPTABLE ).entity( responseMessageModel ).build();
		else
			return Response.status( Status.ACCEPTED ).entity( responseMessageModel ).build();
	}

}