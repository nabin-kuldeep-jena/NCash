package com.asjngroup.ncash.order.service.models;

import java.math.BigDecimal;
import java.util.List;

import com.asjngroup.ncash.common.service.models.AbstractResponseModel;

public class OrderTblModel extends AbstractResponseModel
{
	private Integer stbId = 0;
	private BigDecimal odrTotalAmt;
	private BigDecimal odrSubtotalAmt;
	private List<OrderItemModel> orderItems;

	public Integer getStbId()
	{
		return stbId;
	}

	public void setStbId( Integer stbId )
	{
		this.stbId = stbId;
	}

	public BigDecimal getOdrTotalAmt()
	{
		return odrTotalAmt;
	}

	public void setOdrTotalAmt( BigDecimal odrTotalAmt )
	{
		this.odrTotalAmt = odrTotalAmt;
	}

	public List<OrderItemModel> getOrderItems()
	{
		return orderItems;
	}

	public void setOrderItems( List<OrderItemModel> orderItems )
	{
		this.orderItems = orderItems;
	}

	public BigDecimal getOdrSubtotalAmt()
	{
		return odrSubtotalAmt;
	}

	public void setOdrSubtotalAmt( BigDecimal odrSubtotalAmt )
	{
		this.odrSubtotalAmt = odrSubtotalAmt;
	}

	public String getOdrExternalRef()
	{
		return odrExternalRef;
	}

	public void setOdrExternalRef( String odrExternalRef )
	{
		this.odrExternalRef = odrExternalRef;
	}

	private String odrExternalRef = "";
}