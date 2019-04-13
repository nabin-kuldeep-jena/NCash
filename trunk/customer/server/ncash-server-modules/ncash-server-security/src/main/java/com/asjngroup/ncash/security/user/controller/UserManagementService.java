package com.asjngroup.ncash.security.user.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.models.RegisterUserModel;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.security.service.models.EmailAccessModel;

public interface UserManagementService
{
	ResponseMessageModel registerUser( RegisterUserModel registerUserModel, MessageContext context );

	ResponseMessageModel verifyOtp( String otpToken, String clientUid, String userName, int urtId, MessageContext context );

	ResponseMessageModel validateMobileNoExist( String mobNo, MessageContext context );

	ResponseMessageModel validateEmailIdExist( String emailId, MessageContext context );

	ResponseMessageModel activateEmail( EmailAccessModel emailModel, MessageContext context );

	ResponseMessageModel forgotPassword( String clientUid, String userName, MessageContext context );

	ResponseMessageModel verifyAndChangePasswordForForgotPassword( String otpToken, String clientUid, String userName, String password, MessageContext context );

	ResponseMessageModel verifyAndChangePassword( String userName, String oldPassword, String password, MessageContext context );
}
