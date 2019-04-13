package com.asjngroup.ncash.common.service.models;

import com.asjngroup.ncash.common.database.hibernate.references.ConnectedUser;
import com.asjngroup.ncash.common.models.UserSession;
import com.asjngroup.ncash.framework.security.model.LoginAttempt;

public class UserDetailsModel
{
	private String appName;

	private String userName;

	private String hashedPassword;

	private String unHashedPassword;

	private String logoutUrl;

	private String locale = "en";

	private String groupSeperator = ",";

	private String decimalSeperator = ".";

	private String orientation = "ltr";

	private LoginAttempt loginAttempt;

	private UserSession userSession = null;

	private String clientIpAddress = null;

	private String clientHostName = null;

	private String sessionId = null;

	private ConnectedUser connectedUser;

	public String getAppName()
	{
		return appName;
	}

	public void setAppName( String appName )
	{
		this.appName = appName;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName( String userName )
	{
		this.userName = userName;
	}

	public String getHashedPassword()
	{
		return hashedPassword;
	}

	public void setHashedPassword( String hashedPassword )
	{
		this.hashedPassword = hashedPassword;
	}

	public String getUnHashedPassword()
	{
		return unHashedPassword;
	}

	public void setUnHashedPassword( String unHashedPassword )
	{
		this.unHashedPassword = unHashedPassword;
	}

	public String getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl( String logoutUrl )
	{
		this.logoutUrl = logoutUrl;
	}

	public String getLocale()
	{
		return locale;
	}

	public void setLocale( String locale )
	{
		this.locale = locale;
	}

	public String getGroupSeperator()
	{
		return groupSeperator;
	}

	public void setGroupSeperator( String groupSeperator )
	{
		this.groupSeperator = groupSeperator;
	}

	public String getDecimalSeperator()
	{
		return decimalSeperator;
	}

	public void setDecimalSeperator( String decimalSeperator )
	{
		this.decimalSeperator = decimalSeperator;
	}

	public String getOrientation()
	{
		return orientation;
	}

	public void setOrientation( String orientation )
	{
		this.orientation = orientation;
	}

	public LoginAttempt getLoginAttempt()
	{
		return loginAttempt;
	}

	public void setLoginAttempt( LoginAttempt loginAttempt )
	{
		this.loginAttempt = loginAttempt;
	}

	public UserSession getUserSession()
	{
		return userSession;
	}

	public void setUserSession( UserSession userSession )
	{
		this.userSession = userSession;
	}

	public String getClientIpAddress()
	{
		return clientIpAddress;
	}

	public void setClientIpAddress( String clientIpAddress )
	{
		this.clientIpAddress = clientIpAddress;
	}

	public String getClientHostName()
	{
		return clientHostName;
	}

	public void setClientHostName( String clientHostName )
	{
		this.clientHostName = clientHostName;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId( String sessionId )
	{
		this.sessionId = sessionId;
	}

	public ConnectedUser getConnectedUser()
	{
		return connectedUser;
	}

	public void setConnectedUser( ConnectedUser connectedUser )
	{
		this.connectedUser = connectedUser;
	}

}
