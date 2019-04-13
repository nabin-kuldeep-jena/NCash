package com.asjngroup.ncash.addons.razorpay.models;

public abstract class AbstractRazorpayModel
{
	private String id;
	private String entity;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getEntity()
	{
		return entity;
	}

	public void setEntity( String entity )
	{
		this.entity = entity;
	}

}
