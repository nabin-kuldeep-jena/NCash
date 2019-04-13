package com.asjngroup.ncash.security.service.models;

public class ChangePassowrdModel
{
	private String clientUid;
	private String otpToken;
	private String usrMobileNo;
	private String newPassword;
	private String usrName;
	private String usrPassword;
	private String oldPassword;
	
	public String getOldPassword()
	{
		return oldPassword;
	}

	public void setOldPassword( String oldPassword )
	{
		this.oldPassword = oldPassword;
	}

	public String getUsrPassword()
	{
		return usrPassword;
	}

	public void setUsrPassword( String usrPassword )
	{
		this.usrPassword = usrPassword;
	}

	public String getUsrName()
	{
		return usrName;
	}

	public void setUsrName( String usrName )
	{
		this.usrName = usrName;
	}

	public String getClientUid()
	{
		return clientUid;
	}

	public void setClientUid( String clientUid )
	{
		this.clientUid = clientUid;
	}

	public String getOtpToken()
	{
		return otpToken;
	}

	public void setOtpToken( String otpToken )
	{
		this.otpToken = otpToken;
	}

	public String getUsrMobileNo()
	{
		return usrMobileNo;
	}

	public void setUsrMobileNo( String usrMobileNo )
	{
		this.usrMobileNo = usrMobileNo;
	}

	public String getNewPassword()
	{
		return newPassword;
	}

	public void setNewPassword( String newPassword )
	{
		this.newPassword = newPassword;
	}

}
