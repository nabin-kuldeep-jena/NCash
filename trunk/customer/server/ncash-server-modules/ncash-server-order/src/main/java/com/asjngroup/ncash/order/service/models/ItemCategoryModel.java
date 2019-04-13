package com.asjngroup.ncash.order.service.models;

import com.asjngroup.ncash.common.service.models.AbstractResponseModel;

public class ItemCategoryModel extends AbstractResponseModel
{
	private String icaName = "";
	private String icaCode = "";
	private Boolean icaTaxableFl = false;
	private Boolean icaCouponsFl = false;
	public String getIcaName()
	{
		return icaName;
	}
	public void setIcaName( String icaName )
	{
		this.icaName = icaName;
	}
	public String getIcaCode()
	{
		return icaCode;
	}
	public void setIcaCode( String icaCode )
	{
		this.icaCode = icaCode;
	}
	public Boolean getIcaTaxableFl()
	{
		return icaTaxableFl;
	}
	public void setIcaTaxableFl( Boolean icaTaxableFl )
	{
		this.icaTaxableFl = icaTaxableFl;
	}
	public Boolean getIcaCouponsFl()
	{
		return icaCouponsFl;
	}
	public void setIcaCouponsFl( Boolean icaCouponsFl )
	{
		this.icaCouponsFl = icaCouponsFl;
	}
}
