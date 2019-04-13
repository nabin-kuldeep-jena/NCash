package com.asjngroup.ncash.framework.security.component;

import org.hibernate.Session;

import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.references.ApplicationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.framework.security.model.LoginAttempt;

public abstract interface PasswordValidationInterface
{
	public abstract boolean validateLoginAttempt(Session txnSession, LoginAttempt paramLoginAttempt, UserTbl paramUserTbl ) throws UserPasswordValidationException;

	public abstract boolean isPasswordValid(Session txnSession, String paramString, List<String> paramList, UserTbl paramUserTbl );

	public abstract boolean isPasswordExpired(LoginAttempt paramLoginAttempt );

	public abstract boolean isLoginAuthenticated( ApplicationTbl paramApplicationTbl );

	public abstract String getDefaultPassword();

	public abstract Integer getWarningResetIntervalDiff(LoginAttempt paramLoginAttempt );

	public boolean doDatabaseBookKeeping( Session txnSession, LoginAttempt loginAttempt, boolean bSuccessFlag, String strMessage );
}