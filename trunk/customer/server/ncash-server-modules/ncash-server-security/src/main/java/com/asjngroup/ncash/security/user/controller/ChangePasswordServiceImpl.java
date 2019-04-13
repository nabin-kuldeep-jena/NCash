package com.asjngroup.ncash.security.user.controller;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.asjngroup.ncash.addons.security.otp.OtpGenerator;
import com.asjngroup.ncash.addons.security.otp.TimeIntervalMode;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.security.login.service.ChangePasswordService;
import com.asjngroup.ncash.security.service.models.ChangePassowrdModel;

public class ChangePasswordServiceImpl implements ChangePasswordService
{
	public ResponseMessageModel updatePassword( ChangePassowrdModel changePasswordModel, MessageContext context )
	{
		Session session = null;
		Transaction txn = null;
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		try
		{
			if ( OtpGenerator.verifyTimeBased( changePasswordModel.getUsrName(), changePasswordModel.getClientUid(), changePasswordModel.getOtpToken(), TimeIntervalMode.TENMIN ) )
			{
				session = HibernateSession.openSession();
				txn = session.beginTransaction();

				List<UserTbl> changePassword = HibernateUtil.query( session, "from UserTbl usr where usr.usrName =:usrName", new String[]
				{ "usrName" }, new Object[]
				{ changePasswordModel.getUsrName() } );

				if ( changePassword.size() == 1 )
				{
					UserTbl usrPassword = changePassword.get( 0 );
					usrPassword.setUsrPassword( changePasswordModel.getNewPassword() );
					session.save( usrPassword );
					txn.commit();

					responseMessageModel.setMessage( "Password Changed Successfully" );
					responseMessageModel.setStatusCode( Status.OK.getStatusCode() );
				}
				else
				{
					responseMessageModel.setMessage( "Error Changing User Password!" );
					responseMessageModel.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
				}
			}
			else
			{
				responseMessageModel.setMessage( "Error Changing User Password!" );
				responseMessageModel.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
			}
		}
		catch ( HibernateException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( session != null && session.isOpen() )
				session.close();
		}
		return responseMessageModel;
	}

}
