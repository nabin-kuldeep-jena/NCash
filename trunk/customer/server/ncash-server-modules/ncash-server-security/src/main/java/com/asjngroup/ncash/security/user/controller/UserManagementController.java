package com.asjngroup.ncash.security.user.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.models.RegisterUserModel;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;
import com.asjngroup.ncash.email.util.sender.EmailSendingFailedEception;
import com.asjngroup.ncash.email.util.sender.EmailTemplateParam;
import com.asjngroup.ncash.email.util.sender.EmailTemplateType;
import com.asjngroup.ncash.email.util.sender.util.NCashEmailUtil;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;
import com.asjngroup.ncash.security.service.models.ChangePassowrdModel;
import com.asjngroup.ncash.security.service.models.EmailAccessModel;
import com.asjngroup.ncash.security.service.models.VerifyOtpModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Api( value = "/user", consumes = "application/json", tags =
{ "User Management" }, produces = "application/json", description = "This api provided user related services like register user,enable user,forgot password" )
public class UserManagementController
{
	@Autowired( required = true )
	UserManagementService userManagementService;

	@POST
	@Path( NCashServicesConstant.REGISTER_USER_URL_RESOURCE )
	@ApiOperation( value = "post operation to create new user .", response = ResponseMessageModel.class )
	public Response registerNewUser( @ApiParam( value = "user Registration model", required = true ) RegisterUserModel registerUserModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.registerUser( registerUserModel, context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@POST
	@Path( NCashServicesConstant.VERIFY_OTP_URL_RESOURCE )
	@ApiOperation( value = "post operation used for verify otp against the user .", response = UserTbl.class )
	public Response verifyOtp(VerifyOtpModel verifyOtpModel, @Context MessageContext context )
	{
		try
		{
			//NCashEmailUtil.sendPlainEmailMessage( "nabinjena.subex@gmail.com", "nabin.kuldeep@gmail.com", "hi", "how are you" );
			EmailTemplateParam emailTemplateParam=new EmailTemplateParam();
			emailTemplateParam.setSubject( "TestEmail" );
			emailTemplateParam.setSenderEmail(  "nabinjena.subex@gmail.com" );
			emailTemplateParam.setRecipientEmail( "nabin.kuldeep@gmail.com");
			emailTemplateParam.setEmailTemplateType( EmailTemplateType.HTML );
			emailTemplateParam.setHtmlTemplateName( "oreder-processed.html" );
			emailTemplateParam.setLocale( Locale.ENGLISH );
			NCashEmailUtil.sendHtmlTemplateEmailMessage( emailTemplateParam );
		}
		catch ( EmailSendingFailedEception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResponseMessageModel responseMessageModel = userManagementService.verifyOtp( verifyOtpModel.getOtpToken(), verifyOtpModel.getClientUid(),verifyOtpModel.getUsername(), verifyOtpModel.getUrtId(), context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@GET
	@Path( NCashServicesConstant.VALIDATE_MOBILE_NO_URL_RESOURCE )
	@ApiOperation( value = "get operation used for validate mobile no exist or not in the system .", response = UserTbl.class )
	public Response validateMobileNo( @ApiParam( value = "mobile no", required = true ) @QueryParam( "mobNo" ) String mobNo, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.validateMobileNoExist( mobNo, context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@GET
	@Path( NCashServicesConstant.VALIDATE_EMAIL_URL_RESOURCE )
	@ApiOperation( value = "get operation used for validate email exist or not in the system .", response = UserTbl.class )
	public Response validateEmail( @ApiParam( value = "email idemail", required = true ) @QueryParam( "emailId" ) String emailId, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.validateEmailIdExist( emailId, context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@POST
	@Path( NCashServicesConstant.ACTIVATE_EMAIL_URL_RESOURCE )
	@ApiOperation( value = "get operation used for activate email eid based on access token .", response = UserTbl.class )
	public Response activateEmail( @ApiParam( value = "email token details", required = true ) EmailAccessModel emailModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.activateEmail( emailModel, context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@POST
	@Path( NCashServicesConstant.FORGOT_PWD_URL_RESOURCE )
	@ApiOperation( value = "post operation used generate otp against the user for forgot password .", response = String.class )
	public Response forgotPassword( @ApiParam( value = "password details", required = true ) ChangePassowrdModel changePassowrdModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.forgotPassword( changePassowrdModel.getClientUid(), changePassowrdModel.getUsrName(), context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@POST
	@Path( NCashServicesConstant.CHANGE_FORGOT_PWD_URL_RESOURCE )
	@ApiOperation( value = "post operation for verify and changed the password .", response = String.class )
	public Response verifyAndChangePasswordForForgotPassword( @ApiParam( value = "password details", required = true ) ChangePassowrdModel changePassowrdModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.verifyAndChangePasswordForForgotPassword( changePassowrdModel.getOtpToken(), changePassowrdModel.getClientUid(), changePassowrdModel.getUsrName(), changePassowrdModel.getUsrPassword(), context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	@POST
	@Path( NCashServicesConstant.CHANGE_PWD_URL_RESOURCE )
	@ApiOperation( value = "post operation for verify and changed the password .", response = String.class )
	public Response verifyAndChangePassword( @ApiParam( value = "password details", required = true ) ChangePassowrdModel changePassowrdModel, @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = userManagementService.verifyAndChangePassword( changePassowrdModel.getUsrName(), changePassowrdModel.getOldPassword(), changePassowrdModel.getUsrPassword(), context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	public UserManagementService getUserManagementService()
	{
		return userManagementService;
	}

	public void setUserManagementService( UserManagementService userManagementService )
	{
		this.userManagementService = userManagementService;
	}
}
