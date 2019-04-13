package com.asjngroup.ncash.email.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class NcashMailAuthenticator extends Authenticator
{
	private String username;
	private String password;

	public NcashMailAuthenticator( Properties properties )
	{
		// get the SMTP username and password properties
		this.username = properties.getProperty( "mail.smtp.user" );
		this.password = properties.getProperty( "mail.smtp.password" );
	}

	public NcashMailAuthenticator( String userName,String password )
	{
		// get the SMTP username and password properties
		this.username = userName;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication()
	{
		// return the authentication username and password
		return new PasswordAuthentication( username, password );
	}
}
