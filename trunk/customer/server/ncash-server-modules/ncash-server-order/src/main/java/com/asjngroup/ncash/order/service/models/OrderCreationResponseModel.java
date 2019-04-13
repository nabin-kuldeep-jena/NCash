package com.asjngroup.ncash.order.service.models;

public class OrderCreationResponseModel
{
	private long orderNo;
	private String ossName;
	private String ossCodee;
	public long getOrderNo()
	{
		return orderNo;
	}

	public void setOrderNo( long orderNo )
	{
		this.orderNo = orderNo;
	}

	public String getOssName()
	{
		return ossName;
	}

	public void setOssName( String ossName )
	{
		this.ossName = ossName;
	}

	public String getOssCodee()
	{
		return ossCodee;
	}

	public void setOssCodee( String ossCodee )
	{
		this.ossCodee = ossCodee;
	}	
	
}
