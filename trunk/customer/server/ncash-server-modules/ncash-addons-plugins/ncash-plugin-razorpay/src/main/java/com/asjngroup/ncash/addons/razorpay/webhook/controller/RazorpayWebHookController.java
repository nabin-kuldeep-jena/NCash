package com.asjngroup.ncash.addons.razorpay.webhook.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.Session;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.asjngroup.ncash.addons.razorpay.models.RazorpayPaymentModel;
import com.asjngroup.ncash.addons.razorpay.models.RazorpayWebHookModel;
import com.asjngroup.ncash.addons.razorpay.webhook.exception.PaymentUpdateException;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Component( value = "razorpayWebHookController" )
@Api( value = "/rzpay", consumes = "application/json", tags =
{ "RZPay" }, produces = "application/json", description = "This api used to verify the payment using zazorpay webhook." )
public class RazorpayWebHookController
{
	@Autowired( required = true )
	RazorpayPaymentService razorpayPaymentService;

	@POST
	@Path( RPServicesConstant.ON_PAYMENT_EVENT )
	@ApiOperation( value = "post operation to manage payemnt status sent by razorpay webhook ", response = UserTbl.class )
	public Response onPaymentEvent( RazorpayWebHookModel rpWebHookModel, @Context MessageContext context )
	{
		RazorpayPaymentModel razorpayPaymentModel = null;
		if ( razorpayPaymentService.isValidRequest( rpWebHookModel, context ) )
		{
			if ( RPServicesConstant.RP_WH_EVENT_PAYMENT_AUTHORIZED.equals( rpWebHookModel.getEvent() ) )
			{
				try
				{
					ResponseMessageModel responseMessageModel = razorpayPaymentService.onPaymentAuthorized( razorpayPaymentModel, context );
					ResponseBuilder.sendAcceptResponse( responseMessageModel );
				}
				catch ( PaymentUpdateException e )
				{
					return ResponseBuilder.sendErrorResponse( "Failed to update payment" );
				}
			}
			else if ( RPServicesConstant.RP_WH_EVENT_PAYMENT_FAILED.equals( rpWebHookModel.getEvent() ) )
			{
				try
				{
					ResponseMessageModel responseMessageModel = razorpayPaymentService.onPaymentFailed( razorpayPaymentModel, context );
					ResponseBuilder.sendAcceptResponse( responseMessageModel );
				}
				catch ( PaymentUpdateException e )
				{
					return ResponseBuilder.sendErrorResponse( "Failed to update payment" );
				}
			}
			else if ( RPServicesConstant.RP_WH_EVENT_PAYMENT_CAPTURED.equals( rpWebHookModel.getEvent() ) )
			{
				try
				{
					ResponseMessageModel responseMessageModel = razorpayPaymentService.onPaymentCaptured( razorpayPaymentModel, context );
					ResponseBuilder.sendAcceptResponse( responseMessageModel );
				}
				catch ( PaymentUpdateException e )
				{
					return ResponseBuilder.sendErrorResponse( "Failed to update payment" );
				}
			}
			else if ( RPServicesConstant.RP_WH_EVENT_ORDER_PAID.equals( rpWebHookModel.getEvent() ) )
			{
				razorpayPaymentService.onOrderPaid( razorpayPaymentModel, context );
			}
			else if ( RPServicesConstant.RP_WH_EVENT_INVOICE_PAID.equals( rpWebHookModel.getEvent() ) )
			{
				razorpayPaymentService.onInvoicePaid( razorpayPaymentModel, context );
			}
			return null;
		}
		else
		{
			return ResponseBuilder.sendErrorResponse( "Unable to verify request .Please check the authentication information" );
		}
	}
}
