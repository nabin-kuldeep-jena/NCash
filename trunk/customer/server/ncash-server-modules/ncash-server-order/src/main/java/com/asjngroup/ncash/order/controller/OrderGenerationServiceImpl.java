package com.asjngroup.ncash.order.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;

import javax.ws.rs.core.Response.Status;

import com.asjngroup.ncash.common.database.helper.SequenceNumberHelper;
import com.asjngroup.ncash.common.database.hibernate.references.ItemCategory;
import com.asjngroup.ncash.common.database.hibernate.references.OrderItem;
import com.asjngroup.ncash.common.database.hibernate.references.OrderStatus;
import com.asjngroup.ncash.common.database.hibernate.references.OrderTbl;
import com.asjngroup.ncash.common.database.hibernate.references.StoreBranch;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.models.UserSession;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.framework.security.helper.UserHelper;
import com.asjngroup.ncash.order.service.models.OrderCreationResponseModel;
import com.asjngroup.ncash.order.service.models.OrderItemModel;
import com.asjngroup.ncash.order.service.models.OrderTblModel;
import com.asjngroup.ncash.order.util.StoreOrderStatus;

public class OrderGenerationServiceImpl implements OrderGenerationService
{

	public OrderCreationResponseModel createNewOrder( OrderTblModel orderTblModel, MessageContext context, ResponseMessageModel errorResponse )
	{
		errorResponse.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
		errorResponse.setMessage( "Something Went's wrong " );

		if ( orderTblModel.getOdrTotalAmt() == null || BigDecimal.ZERO.equals( orderTblModel.getOdrTotalAmt() ) )
			errorResponse.addError( "odrTotalAmt", "Total Amount Can not be zero" );
		else if ( orderTblModel.getOdrSubtotalAmt() == null || BigDecimal.ZERO.equals( orderTblModel.getOdrSubtotalAmt() ) )
			errorResponse.addError( "odrSubtotalAmt", "Sub Total Amount Can not be zero" );
		else if ( orderTblModel.getOdrSubtotalAmt() == null || BigDecimal.ZERO.equals( orderTblModel.getOdrSubtotalAmt() ) )
			errorResponse.addError( "odrSubtotalAmt", "Sub Total Amount Can not be zero" );
		else if ( orderTblModel.getOrderItems() == null || orderTblModel.getOrderItems().size() == 0 )
			errorResponse.addError( "orderItems", "There is no items in the cart " );
		else
		{
			Session session = null;
			Transaction txn = null;
			try
			{
				session = HibernateSession.openSession();
				txn = session.beginTransaction();
				long orderNumber = SequenceNumberHelper.generateId( OrderTbl.class );
				OrderStatus newOrderStatus = ( OrderStatus ) HibernateUtil.query( session, "from OrderStatus oss where oss.ossCode=:ossCode", "ossCode", StoreOrderStatus.NC.toString() ).get( 0 );
				OrderTbl orderTbl = HibernateSession.createObject( OrderTbl.class, true );
				UserSession currentUserSession = UserHelper.getCurrentUserSession( context );
				orderTbl.setUserLoginInfo( currentUserSession.getUserLoginInfo() );
				orderTbl.setStoreBranch( HibernateSession.get( StoreBranch.class, orderTblModel.getStbId() ) );
				orderTbl.setCreatedDttm( new DateTime() );
				orderTbl.setOdrUpdatedDttm( new DateTime() );
				orderTbl.setOdrTotalAmt( orderTblModel.getOdrTotalAmt() );
				orderTbl.setOdrSubtotalAmt( orderTblModel.getOdrSubtotalAmt() );
				orderTbl.setOrderStatus( newOrderStatus );
				orderTbl.setOdrNo( orderNumber );
				session.save( orderTbl );
				for ( OrderItemModel orderItemModel : orderTblModel.getOrderItems() )
				{
					ItemCategory itemCategory = HibernateSession.createObject( ItemCategory.class, false );
					itemCategory.setIcaId( orderItemModel.getItemCategory().getId() );
					OrderItem orderItem = HibernateSession.createObject( OrderItem.class, true );
					orderItem.setOrderTbl( orderTbl );
					orderItem.setOitName( orderItemModel.getOitName() );
					orderItem.setOitDisplayValue( orderItemModel.getOitDisplayValue() );
					orderItem.setOitItemPrice( orderItemModel.getOitItemPrice() );
					orderItem.setOitOfferedPrice( orderItemModel.getOitOfferedPrice() );
					orderItem.setOitBarcode( orderItemModel.getOitBarcode() );
					orderItem.setOitQuantity( orderItemModel.getOitQuantity() );
					orderItem.setItemCategory( itemCategory );
					session.save( orderItem );
				}
				OrderCreationResponseModel responseModel = new OrderCreationResponseModel();
				responseModel.setOrderNo( orderNumber );
				responseModel.setOssName( newOrderStatus.getOssName() );
				responseModel.setOssCodee( newOrderStatus.getOssCode() );
				txn.commit();
				return responseModel;

			}
			catch ( Exception e )
			{
				errorResponse.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
				errorResponse.setMessage( "Failed to connect to Server" );
				if ( txn != null )
					txn.rollback();
			}
			finally
			{
				HibernateSession.closeSession( session );
			}
		}
		return null;
	}

}
