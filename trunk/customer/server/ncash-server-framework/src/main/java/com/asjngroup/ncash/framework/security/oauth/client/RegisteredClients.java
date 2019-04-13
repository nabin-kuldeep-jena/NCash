package com.asjngroup.ncash.framework.security.oauth.client;

import org.apache.cxf.rs.security.oauth2.common.Client;

import java.util.Collection;

public class RegisteredClients
{
	private Collection<Client> clients;

	public RegisteredClients( Collection<Client> clients )
	{
		this.clients = clients;
	}

	public Collection<Client> getClients()
	{
		return clients;
	}

}