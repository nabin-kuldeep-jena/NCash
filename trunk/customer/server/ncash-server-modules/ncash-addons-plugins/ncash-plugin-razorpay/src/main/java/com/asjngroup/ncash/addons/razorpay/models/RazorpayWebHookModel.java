package com.asjngroup.ncash.addons.razorpay.models;

public class RazorpayWebHookModel extends AbstractRazorpayModel
{
	private String event;
	private String entity;
	private String[] contains;

	public String getEvent()
	{
		return event;
	}

	public void setEvent( String event )
	{
		this.event = event;
	}

	public String getEntity()
	{
		return entity;
	}

	public void setEntity( String entity )
	{
		this.entity = entity;
	}

	public String[] getContains()
	{
		return contains;
	}

	public void setContains( String[] contains )
	{
		this.contains = contains;
	}

}
