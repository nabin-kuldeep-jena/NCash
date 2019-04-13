package com.asjngroup.ncash.framework.security.model;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;

public class LoginAttempt
{
	private UserTbl userTbl;
	private String hashedPassword;
	private boolean manualLogin;
	private boolean success = true;

	private String message = null;
	private int messageId;
	private String[] messageParameter;
	private boolean disableUserTbl = false;

	private boolean isChangePasswordApplicable = false;

	private boolean passwordExpired = false;

	private String strClientIpAddress = null;

	private String strClientHostName = null;

	private String givenUserName = null;

	private String logonMessageText = null;
	private int usrLoginId;
	private boolean userInPasswordWarningInterval = false;

	public int getUsrLoginId()
	{
		return this.usrLoginId;
	}

	public void setUsrLoginId( int usrLoginId )
	{
		this.usrLoginId = usrLoginId;
	}

	public LoginAttempt( UserTbl userTbl, String hashedPassword, boolean manualLogin )
	{
		this.userTbl = userTbl;
		this.hashedPassword = hashedPassword;
		this.manualLogin = manualLogin;
	}

	public void failedLogin( String msg )
	{
		setSuccess( false );
		setMessage( msg );
	}

	public void disableLogin( String msg )
	{
		failedLogin( msg );
		setDisableUserTbl( true );
	}

	public UserTbl getUserTbl()
	{
		return this.userTbl;
	}

	public String getHashedPassword()
	{
		return this.hashedPassword;
	}

	public void setHashedPassword( String hashedPassword )
	{
		this.hashedPassword = hashedPassword;
	}

	public boolean isManualLogin()
	{
		return this.manualLogin;
	}

	public boolean isSuccess()
	{
		return this.success;
	}

	public void setSuccess( boolean success )
	{
		this.success = success;
	}

	public String getMessage()
	{
		return this.message;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public int getMessageId()
	{
		return this.messageId;
	}

	public void setMessageId( int messageId )
	{
		this.messageId = messageId;
	}

	public String[] getMessageParameter()
	{
		return this.messageParameter;
	}

	public void setMessageParameter( String[] messageParameter )
	{
		this.messageParameter = messageParameter;
	}

	public boolean isDisableUserTbl()
	{
		return this.disableUserTbl;
	}

	public void setDisableUserTbl( boolean disableUserTbl )
	{
		this.disableUserTbl = disableUserTbl;
	}

	public void setChangePasswordApplicable( boolean isChangePasswordApplicable )
	{
		this.isChangePasswordApplicable = isChangePasswordApplicable;
	}

	public boolean isChangePasswordApplicable()
	{
		return this.isChangePasswordApplicable;
	}

	public void setClientIpAddress( String strClientIpAddress )
	{
		this.strClientIpAddress = strClientIpAddress;
	}

	public String getClientIpAddress()
	{
		return this.strClientIpAddress;
	}

	public void setClientHostName( String strClientHostName )
	{
		this.strClientHostName = strClientHostName;
	}

	public String getClientHostName()
	{
		return this.strClientHostName;
	}

	public void setGivenUserName( String givenUserName )
	{
		this.givenUserName = givenUserName;
	}

	public String getGivenUserName()
	{
		return this.givenUserName;
	}

	public void setPasswordExpired( boolean passwordExpired )
	{
		this.passwordExpired = passwordExpired;
	}

	public boolean isPasswordExpired()
	{
		return this.passwordExpired;
	}

	public void setUserInPasswordWarningInterval( boolean userInPasswordWarningInterval )
	{
		this.userInPasswordWarningInterval = userInPasswordWarningInterval;
	}

	public boolean isUserInPasswordWarningInterval()
	{
		return this.userInPasswordWarningInterval;
	}

	public void setLogonMessageText( String logonMessageText )
	{
		this.logonMessageText = logonMessageText;
	}

	public String getLogonMessageText()
	{
		return this.logonMessageText;
	}
}