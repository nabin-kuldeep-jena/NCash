package com.asjngroup.ncash.security.login.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.security.service.models.ChangePassowrdModel;

public interface ChangePasswordService
{
	ResponseMessageModel updatePassword( ChangePassowrdModel changePasswordModel, MessageContext context );
}
