package com.asjngroup.ncash.addons.razorpay.webhook.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SignatureException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import com.asjngroup.ncash.addons.razorpay.models.RazorpayPaymentModel;
import com.asjngroup.ncash.addons.razorpay.models.RazorpayWebHookModel;
import com.asjngroup.ncash.addons.razorpay.webhook.exception.PaymentUpdateException;
import com.asjngroup.ncash.common.database.helper.SequenceNumberHelper;
import com.asjngroup.ncash.common.database.hibernate.references.Currency;
import com.asjngroup.ncash.common.database.hibernate.references.OrderTbl;
import com.asjngroup.ncash.common.database.hibernate.references.PaymentStatus;
import com.asjngroup.ncash.common.database.hibernate.references.PaymentTransaction;
import com.asjngroup.ncash.common.database.hibernate.references.TransactionSource;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.framework.security.helper.SHASecurityUtil;

public class RazorpayPaymentServiceImpl implements RazorpayPaymentService
{

	public boolean isValidRequest( RazorpayWebHookModel rpWebHookModel, MessageContext context )
	{
		try
		{
			String configuredSecretKey = "";
			List<String> skValues = context.getHttpHeaders().getRequestHeaders().get( RPServicesConstant.RP_WEBHOOK_SIGNATURE_KEY );
			if ( skValues == null || skValues.isEmpty() )
				return false;
			else
			{
				String rpWHSecretKey;
				rpWHSecretKey = SHASecurityUtil.decryptSHA256( skValues.get( 0 ), "126387216387216" );
				if ( configuredSecretKey.equals( rpWHSecretKey ) )
					return true;
				else
					return false;
			}
		}
		catch ( SignatureException e )
		{
			return false;
		}
	}

	public ResponseMessageModel onPaymentAuthorized( RazorpayPaymentModel razorpayPaymentModel, MessageContext context ) throws PaymentUpdateException
	{
		try
		{

			Session session = HibernateSession.openSession();
			List<OrderTbl> orderTbls = HibernateUtil.query( session, "from OrderTbl where odrId=:odrId", "odrId", razorpayPaymentModel.getOrder_id() );
			if ( orderTbls != null && !orderTbls.isEmpty() )
			{
				OrderTbl orderTbl = orderTbls.get( 0 );
				PaymentTransaction paymentTransaction = orderTbl.getPaymentTransaction();
				if ( paymentTransaction == null )
				{
					HttpServletRequest httpServletRequest = context.getHttpServletRequest();
					InetAddress iaddr = null;
					iaddr = InetAddress.getByName( httpServletRequest.getRemoteAddr() );
					createNewpaymentTransaction( orderTbl, session, "Authorized", razorpayPaymentModel, httpServletRequest.getRemoteAddr(), iaddr.getCanonicalHostName() );
				}
				else
				{
					updateTransactionStatus( paymentTransaction, session, "Authorized" );
				}
			}
		}
		catch ( UnknownHostException | HibernateException e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		catch ( Exception e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.ACCEPTED.getStatusCode() );
		responseMessageModel.setMessage( "Payment Updated Sucessfully" );
		return responseMessageModel;
	}

	private boolean createNewpaymentTransaction( OrderTbl orderTbl, Session session, String pssCode, RazorpayPaymentModel razorpayPaymentModel, String sourceId, String hostName )
	{
		long txnNumber = SequenceNumberHelper.generateId( PaymentTransaction.class );
		PaymentStatus paymentStatus = HibernateUtil.queryExpectOneRow( session, "from PaymentStatus  where pssCode=:pssCode", "pssCode", pssCode );
		Currency currency = HibernateUtil.queryExpectOneRow( session, "from Currency  where curCode=:curCode", "curCode", razorpayPaymentModel.getCurrency() );
		TransactionSource transactionSource = HibernateUtil.queryExpectOneRow( session, "from TransactionSource  where curCode=:tsoCode", "tsoCode", RPServicesConstant.TRANSACTION_SOURCE_PREFIX + razorpayPaymentModel.getMethod() );
		PaymentTransaction paymentTransaction = HibernateSession.createObject( PaymentTransaction.class, true );
		paymentTransaction.setPttTxnNo( txnNumber );
		paymentTransaction.setTransactionSource( transactionSource );
		paymentTransaction.setPttSourceIp( sourceId );
		paymentTransaction.setPttExtRef( razorpayPaymentModel.getId() );
		paymentTransaction.setPttAmount( new BigDecimal( razorpayPaymentModel.getAmount() / 100 ) );
		paymentTransaction.setPttDttm( new DateTime() );
		paymentTransaction.setCurrency( currency );
		paymentTransaction.setPaymentStatus( paymentStatus );
		paymentTransaction.setPttDescription( "RAZORPAY~" + sourceId + "~" + hostName );
		session.saveOrUpdate( paymentTransaction );
		return true;

	}

	private void updateTransactionStatus( PaymentTransaction paymentTransaction, Session session, String pssCode )
	{
		PaymentStatus paymentStatus = HibernateUtil.queryExpectOneRow( session, "from PaymentStatus  where pssCode=:pssCode", "pssCode", pssCode );
		paymentTransaction.setPaymentStatus( paymentStatus );
		session.update( paymentTransaction );
	}

	public ResponseMessageModel onPaymentFailed( RazorpayPaymentModel razorpayPaymentModel, MessageContext context ) throws PaymentUpdateException
	{
		try
		{

			Session session = HibernateSession.openSession();
			List<OrderTbl> orderTbls = HibernateUtil.query( session, "from OrderTbl where odrId=:odrId", "odrId", razorpayPaymentModel.getOrder_id() );
			if ( orderTbls != null && !orderTbls.isEmpty() )
			{
				OrderTbl orderTbl = orderTbls.get( 0 );
				PaymentTransaction paymentTransaction = orderTbl.getPaymentTransaction();
				if ( paymentTransaction == null )
				{
					HttpServletRequest httpServletRequest = context.getHttpServletRequest();
					InetAddress iaddr = null;
					iaddr = InetAddress.getByName( httpServletRequest.getRemoteAddr() );
					createNewpaymentTransaction( orderTbl, session, "Failed", razorpayPaymentModel, httpServletRequest.getRemoteAddr(), iaddr.getCanonicalHostName() );
				}
				else
				{
					updateTransactionStatus( paymentTransaction, session, "Failed" );
				}
			}
		}
		catch ( UnknownHostException | HibernateException e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		catch ( Exception e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.ACCEPTED.getStatusCode() );
		responseMessageModel.setMessage( "Payment Updated Sucessfully" );
		return responseMessageModel;
	}

	public ResponseMessageModel onPaymentCaptured( RazorpayPaymentModel razorpayPaymentModel, MessageContext context ) throws PaymentUpdateException
	{
		try
		{

			Session session = HibernateSession.openSession();
			List<OrderTbl> orderTbls = HibernateUtil.query( session, "from OrderTbl where odrId=:odrId", "odrId", razorpayPaymentModel.getOrder_id() );
			if ( orderTbls != null && !orderTbls.isEmpty() )
			{
				OrderTbl orderTbl = orderTbls.get( 0 );
				PaymentTransaction paymentTransaction = orderTbl.getPaymentTransaction();
				if ( paymentTransaction == null )
				{
					HttpServletRequest httpServletRequest = context.getHttpServletRequest();
					InetAddress iaddr = null;
					iaddr = InetAddress.getByName( httpServletRequest.getRemoteAddr() );
					createNewpaymentTransaction( orderTbl, session, "Captured", razorpayPaymentModel, httpServletRequest.getRemoteAddr(), iaddr.getCanonicalHostName() );
				}
				else
				{
					updateTransactionStatus( paymentTransaction, session, "Captured" );
				}
			}
		}
		catch ( UnknownHostException | HibernateException e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		catch ( Exception e )
		{
			throw new PaymentUpdateException( " Failed to create or update payment ", e );
		}
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.ACCEPTED.getStatusCode() );
		responseMessageModel.setMessage( "Payment Updated Sucessfully" );
		return responseMessageModel;
	}

	public ResponseMessageModel onOrderPaid( RazorpayPaymentModel razorpayPaymentModel, MessageContext context )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResponseMessageModel onInvoicePaid( RazorpayPaymentModel razorpayPaymentModel, MessageContext context )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
