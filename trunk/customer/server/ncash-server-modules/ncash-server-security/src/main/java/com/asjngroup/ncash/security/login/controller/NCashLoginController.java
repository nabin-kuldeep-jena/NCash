package com.asjngroup.ncash.security.login.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.models.UserCredentialModel;
import com.asjngroup.ncash.common.service.models.UserDetailsModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;
import com.asjngroup.ncash.common.util.NCashConstant;
import com.asjngroup.ncash.framework.security.controller.AbstactLoginController;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;
import com.asjngroup.ncash.security.login.service.AuthenticationService;
import com.asjngroup.ncash.security.service.models.UserTblModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Component( value = "loginController" )
@Api( value = "/security", consumes = "application/json", tags =
{ "User Access" }, produces = "application/json", description = "This api provided security related services like authentication,authorizaton ." )
public class NCashLoginController extends AbstactLoginController
{
	@Autowired( required = true )
	AuthenticationService authenticationService;

	@POST
	@Path( NCashServicesConstant.AUTHENTICATE_USER_URL_RESOURCE )
	@ApiOperation( value = "post operation for authenticate user with Response user details", response = UserTbl.class )
	public Response authenticateUser( @ApiParam( value = "user credential model", required = true ) UserCredentialModel model, @Context MessageContext context )
	{

		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );

		try
		{

			if ( model.getUsrName() == null && model.getPassword() == null )
			{
				responseMessageModel.setMessage( "Username, Password and Application are mandatory for login" );
			}
			else
			{
				UserDetailsModel userDetailsModel = new UserDetailsModel();

				userDetailsModel.setAppName( "NCASH" );
				userDetailsModel.setUserName( model.getUsrName() );
				userDetailsModel.setUnHashedPassword( model.getPassword() );
				HttpServletRequest httpServletRequest = context.getHttpServletRequest();
				userDetailsModel.setClientIpAddress( httpServletRequest.getRemoteAddr() );
				InetAddress iaddr;
				iaddr = InetAddress.getByName( httpServletRequest.getRemoteAddr() );
				userDetailsModel.setClientHostName( iaddr.getCanonicalHostName() );
				userDetailsModel.setSessionId( httpServletRequest.getSession().getId() );
				List<UserTblModel> userTbls = new ArrayList<UserTblModel>( 1 );

				if ( authenticationService.doLoginAction( responseMessageModel, userTbls, userDetailsModel ) )
				{
					//To avoid Session Fixation
					httpServletRequest.getSession().invalidate();
					httpServletRequest.getSession( true );
					httpServletRequest.getSession().setAttribute( NCashConstant.USER_SESSION, userDetailsModel.getUserSession() );
					String protocol = System.getProperty( "ncash.HTTPProtocol" );

					if ( protocol == null )
					{
						protocol = "http";
					}

					String protocolPort = System.getProperty( "ncash.HTTPProtocolPort" );

					if ( protocolPort == null )
					{
						protocolPort = httpServletRequest.getServerPort() + "";
					}
					String contextPath = httpServletRequest.getContextPath();

					if ( !contextPath.endsWith( "/" ) )
					{
						contextPath = contextPath + '/';
					}
					return ResponseBuilder.sendAcceptResponseForEntity( userTbls.get( 0 ) );
				}
				httpServletRequest.getSession().removeAttribute( NCashConstant.USER_SESSION );
				httpServletRequest.getSession().invalidate();
			}
		}
		catch ( UnknownHostException e )
		{
			responseMessageModel.setMessage( "Error while fetching user information" );
			responseMessageModel.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
		}
		return ResponseBuilder.sendResponse( responseMessageModel );

	}

	public AuthenticationService getAuthenticationService()
	{
		return authenticationService;
	}

	public void setAuthenticationService( AuthenticationService authenticationService )
	{
		this.authenticationService = authenticationService;
	}

}
