package com.asjngroup.ncash.payment.service;

import org.joda.time.DateTime;

public interface NCashPaymentService
{
	void getAllPayments();
	void getPaymentById(int paymentId);
	void capturePaymentById(int paymentId);
	void getRefundPaymentById(int paymentId);
	void getRefundPaymentByRefId(int refId);
	void getAllRefundedPaymentsById(int paymentId);
	void getRefundedPaymentsByRefId(int paymentId,int refundId);
	void getRefundDetailsByDate(DateTime from,DateTime to,Integer count,Integer skip);
}
