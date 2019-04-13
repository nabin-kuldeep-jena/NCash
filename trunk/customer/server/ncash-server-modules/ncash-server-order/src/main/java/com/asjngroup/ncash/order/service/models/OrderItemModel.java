package com.asjngroup.ncash.order.service.models;

import java.math.BigDecimal;

import com.asjngroup.ncash.common.service.models.AbstractResponseModel;

public class OrderItemModel extends AbstractResponseModel
{
	private OrderTblModel orderTbl;
	private String oitName;
	private String oitDisplayValue;
	private BigDecimal oitItemPrice;
	private BigDecimal oitOfferedPrice;
	private String oitBarcode;
	private Integer oitQuantity;
	private ItemCategoryModel itemCategory;
	public ItemCategoryModel getItemCategory()
	{
		return itemCategory;
	}
	public void setItemCategory( ItemCategoryModel itemCategory )
	{
		this.itemCategory = itemCategory;
	}
	public OrderTblModel getOrderTbl()
	{
		return orderTbl;
	}
	public void setOrderTbl( OrderTblModel orderTbl )
	{
		this.orderTbl = orderTbl;
	}
	public String getOitName()
	{
		return oitName;
	}
	public void setOitName( String oitName )
	{
		this.oitName = oitName;
	}
	public String getOitDisplayValue()
	{
		return oitDisplayValue;
	}
	public void setOitDisplayValue( String oitDisplayValue )
	{
		this.oitDisplayValue = oitDisplayValue;
	}
	public BigDecimal getOitItemPrice()
	{
		return oitItemPrice;
	}
	public void setOitItemPrice( BigDecimal oitItemPrice )
	{
		this.oitItemPrice = oitItemPrice;
	}
	public BigDecimal getOitOfferedPrice()
	{
		return oitOfferedPrice;
	}
	public void setOitOfferedPrice( BigDecimal oitOfferedPrice )
	{
		this.oitOfferedPrice = oitOfferedPrice;
	}
	public String getOitBarcode()
	{
		return oitBarcode;
	}
	public void setOitBarcode( String oitBarcode )
	{
		this.oitBarcode = oitBarcode;
	}
	public Integer getOitQuantity()
	{
		return oitQuantity;
	}
	public void setOitQuantity( Integer oitQuantity )
	{
		this.oitQuantity = oitQuantity;
	}
}
