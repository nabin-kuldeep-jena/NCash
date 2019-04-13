package com.asjngroup.ncash.security.login.service;

import java.util.List;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.models.UserDetailsModel;
import com.asjngroup.ncash.security.service.models.UserTblModel;

public interface AuthenticationService
{
	public boolean doLoginAction( ResponseMessageModel errorResponse, List<UserTblModel> userTbls, UserDetailsModel userDetailsModel );

}
