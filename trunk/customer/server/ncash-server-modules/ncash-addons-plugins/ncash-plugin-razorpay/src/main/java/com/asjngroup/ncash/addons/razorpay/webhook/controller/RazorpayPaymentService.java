package com.asjngroup.ncash.addons.razorpay.webhook.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.addons.razorpay.models.RazorpayPaymentModel;
import com.asjngroup.ncash.addons.razorpay.models.RazorpayWebHookModel;
import com.asjngroup.ncash.addons.razorpay.webhook.exception.PaymentUpdateException;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;

public interface RazorpayPaymentService
{

	ResponseMessageModel onPaymentAuthorized(RazorpayPaymentModel razorpayPaymentModel, MessageContext context) throws PaymentUpdateException;

	boolean isValidRequest( RazorpayWebHookModel rpWebHookModel, MessageContext context );

	ResponseMessageModel onPaymentFailed( RazorpayPaymentModel razorpayPaymentModel, MessageContext context ) throws PaymentUpdateException;

	ResponseMessageModel onPaymentCaptured( RazorpayPaymentModel razorpayPaymentModel, MessageContext context ) throws PaymentUpdateException;

	ResponseMessageModel onOrderPaid( RazorpayPaymentModel razorpayPaymentModel, MessageContext context );
	
	ResponseMessageModel onInvoicePaid( RazorpayPaymentModel razorpayPaymentModel, MessageContext context );


}
