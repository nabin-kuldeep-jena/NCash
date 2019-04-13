package com.asjngroup.ncash.framework.security.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.asjngroup.ncash.common.service.models.UserCredentialModel;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;

public interface LoginController
{
	@POST
	@Path( NCashServicesConstant.LOGIN_URL_RESOURCE )
	public Response authenticateUser(UserCredentialModel userCredentialModel, @Context MessageContext context);
}
