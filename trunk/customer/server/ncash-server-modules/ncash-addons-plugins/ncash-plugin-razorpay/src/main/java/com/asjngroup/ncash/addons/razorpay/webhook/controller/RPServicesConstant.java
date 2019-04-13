package com.asjngroup.ncash.addons.razorpay.webhook.controller;

public class RPServicesConstant
{

	public static final String ON_PAYMENT_EVENT = "paymentEvent";
	public static final String RP_WEBHOOK_SIGNATURE_KEY = "X-Razorpay-Signature";
	public static final String TRANSACTION_SOURCE_PREFIX = "RP:";
	
	public static final String RP_WH_EVENT_PAYMENT_AUTHORIZED  = "payment.authorized";
	public static final String RP_WH_EVENT_PAYMENT_FAILED  = "payment.failed";
	public static final String RP_WH_EVENT_PAYMENT_CAPTURED  = "payment.captured";
	public static final String RP_WH_EVENT_ORDER_PAID  = "order.paid";
	public static final String RP_WH_EVENT_INVOICE_PAID  = "invoice.paid";

}
