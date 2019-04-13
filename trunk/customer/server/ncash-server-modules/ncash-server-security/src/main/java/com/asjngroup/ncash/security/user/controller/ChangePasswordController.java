package com.asjngroup.ncash.security.user.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;
import com.asjngroup.ncash.security.login.service.ChangePasswordService;
import com.asjngroup.ncash.security.service.models.ChangePassowrdModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Component( value = "loginController" )
@Api( value = "/changePassword", consumes = "application/json", tags =
{ "User Access" }, produces = "application/json", description = "This api allows user to change password." )
public class ChangePasswordController
{
	@Autowired( required = true )
	ChangePasswordService changePasswordService;

	public ChangePasswordService getChangePasswordService()
	{
		return changePasswordService;
	}

	public void setChangePasswordService( ChangePasswordService changePasswordService )
	{
		this.changePasswordService = changePasswordService;
	}

	@POST
	@ApiOperation( value = "updates user password", response = ResponseMessageModel.class )
	public Response updateUserPassword( @ApiParam( value = "get password model", required = true ) ChangePassowrdModel changePasswordModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = changePasswordService.updatePassword( changePasswordModel, context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

}
