package com.asjngroup.ncash.payment.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;

public class PaymentServiceImpl implements PaymentService
{

	public void verifyPayment( String gatewayPaymentId, Double paidAmount, Long orderId, MessageContext context, ResponseMessageModel responseMessageModel )
	{
		
		
	}

}
