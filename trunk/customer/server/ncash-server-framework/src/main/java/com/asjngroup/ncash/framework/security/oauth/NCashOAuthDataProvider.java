package com.asjngroup.ncash.framework.security.oauth;

import org.apache.cxf.rs.security.oauth2.grants.code.DefaultEncryptingCodeDataProvider;

public class NCashOAuthDataProvider extends DefaultEncryptingCodeDataProvider
{

	public NCashOAuthDataProvider() throws Exception
	{
		super("AES", 192);
	}

}