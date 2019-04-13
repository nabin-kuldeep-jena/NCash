package com.asjngroup.ncash.security.service.models;

import com.asjngroup.ncash.common.service.models.AbstractResponseModel;

public class VerifyOtpModel extends AbstractResponseModel
{
	private String otpToken;
	private String clientUid;
	private String username;
	private int urtId = -1;
	public String getOtpToken()
	{
		return otpToken;
	}
	public void setOtpToken( String otpToken )
	{
		this.otpToken = otpToken;
	}
	public String getClientUid()
	{
		return clientUid;
	}
	public void setClientUid( String clientUid )
	{
		this.clientUid = clientUid;
	}
	public String getUsername()
	{
		return username;
	}
	public void setUsername( String userName )
	{
		this.username = userName;
	}
	public int getUrtId()
	{
		return urtId;
	}
	public void setUrtId( int urtId )
	{
		this.urtId = urtId;
	}
	

}
