package com.asjngroup.ncash.security.user.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.asjngroup.ncash.addons.security.otp.OtpGenerator;
import com.asjngroup.ncash.addons.security.otp.TimeIntervalMode;
import com.asjngroup.ncash.common.database.hibernate.references.ApplicationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.CityTbl;
import com.asjngroup.ncash.common.database.hibernate.references.EmailValidationToken;
import com.asjngroup.ncash.common.database.hibernate.references.UserApplicationAccess;
import com.asjngroup.ncash.common.database.hibernate.references.UserPassword;
import com.asjngroup.ncash.common.database.hibernate.references.UserRegistrationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.models.RegisterUserModel;
import com.asjngroup.ncash.common.properties.NCashServerProperties;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.util.DateHelper;
import com.asjngroup.ncash.framework.security.component.PasswordValidationInterface;
import com.asjngroup.ncash.framework.security.helper.SecurityHelper;
import com.asjngroup.ncash.framework.util.sms.SmsSenderUtil;
import com.asjngroup.ncash.security.dao.EmailValidationTokenDAO;
import com.asjngroup.ncash.security.dao.UserDAO;
import com.asjngroup.ncash.security.service.models.EmailAccessModel;

public class UserManagementServiceImpl implements UserManagementService
{

	@Autowired( required = true )
	@Qualifier( "passwordValidation" )
	PasswordValidationInterface passwordValidation;

	public ResponseMessageModel registerUser( RegisterUserModel registerUserModel, MessageContext context )
	{
		ResponseMessageModel response = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		Session session = null;
		Transaction transaction = null;
		try
		{
			session = HibernateSession.openSession();
			transaction = session.beginTransaction();
			response = validateUser( session, registerUserModel, context );
			if ( response == null )
			{
				response = new ResponseMessageModel( Status.ACCEPTED.getStatusCode() );
				UserRegistrationTbl userRegistrationTbl = createUserRegistrationEntry( registerUserModel );
				session.save( userRegistrationTbl );
				String otpKey = OtpGenerator.generateTimeBasedOtp( registerUserModel.getUrtName(), registerUserModel.getUidToken(), TimeIntervalMode.TENMIN );
				String otpMessage = " your NCASH one time password for registration is " + otpKey;
				if ( NCashServerProperties.isDevMode() )
					System.out.println( otpMessage );
				else
					SmsSenderUtil.sendOtpSms( registerUserModel.getUrtMobNo(), userRegistrationTbl.getCityTbl().getState().getCountryTbl().getCtrDialCode(),otpMessage, "NCAOTP" );
				response.setMessage( "User Register successfully.send the otp to respective mobile no please verify it." );
				response.addSucessRecord( "urtId", String.valueOf( userRegistrationTbl.getId() ) );
				response.setStatusCode( Status.ACCEPTED.getStatusCode() );

			}
			transaction.commit();
		}
		catch ( Exception e )
		{
			response.setMessage( "Failed to register user" );
			response.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
			if ( transaction != null )
				transaction.rollback();
		}
		finally
		{
			HibernateSession.closeSession( session );
		}
		return response;
	}

	private UserRegistrationTbl createUserRegistrationEntry( RegisterUserModel registerUserModel )
	{
		//CountryTbl country=HibernateSession.queryExpectOneRow( "from CountryTbl ctr where ctr.ctrDialCode=:ctrDialCode", "ctrDialCode",registerUserModel.getUrtCountryNoCode());
		CityTbl city=HibernateSession.queryExpectOneRow( "from CityTbl cty where cty.ctyId=:ctyId", "ctyId",registerUserModel.getCtyId());
		//registerUserModel.setUrtCountryNoCode(city.getState().getCountryTbl().getCtrDialCode());
		UserRegistrationTbl userRegistrationTbl = HibernateSession.createObject( UserRegistrationTbl.class, true );
		userRegistrationTbl.setUrtName( registerUserModel.getUrtName() );
		String hashedPassword = SecurityHelper.hashPassword( registerUserModel.getUrtPassword() );
		userRegistrationTbl.setUrtPassword( hashedPassword );
		userRegistrationTbl.setUrtForename( registerUserModel.getUrtForename() );
		userRegistrationTbl.setUrtSurname( registerUserModel.getUrtSurname() );
		userRegistrationTbl.setUrtEmailAddress( registerUserModel.getUrtEmailAddress() );
		userRegistrationTbl.setUrtMobNo( registerUserModel.getUrtMobNo() );
		userRegistrationTbl.setUrtDescription( "NCASH~AppUser" );
		userRegistrationTbl.setCityTbl( city );
		return userRegistrationTbl;
	}

	private ResponseMessageModel validateUser( Session session, RegisterUserModel registerUserModel, MessageContext context )
	{
		List<UserTbl> userNames = UserDAO.getUniqueUser( session, registerUserModel.getUrtName(), registerUserModel.getUrtMobNo(), registerUserModel.getUrtEmailAddress() );
		if ( !userNames.isEmpty() )
		{
			ResponseMessageModel response = new ResponseMessageModel( Status.NOT_ACCEPTABLE.getStatusCode() );
			UserTbl userTbl = userNames.get( 0 );

			if ( userTbl.getUsrMobNo().equals( registerUserModel.getUrtMobNo() ) )
			{
				response.setMessage( "Mobile Number is already registered " );
				response.addError( "usrMobNo", "Mobile Number is already registered " );
			}
			else if ( userTbl.getUsrEmailAddress().equals( registerUserModel.getUrtEmailAddress() ) )
			{
				response.setMessage( "Email Id is already registered " );
				response.addError( "usrEmailAddress", "Email Id is already registered" );
			}
			else if ( userTbl.getUsrName().equals( registerUserModel.getUrtName() ) )
			{
				response.setMessage( "Username is already registered " );
				response.addError( "usrName", "Username is already registered " );
			}
			return response;
		}
		return null;
	}

	public ResponseMessageModel verifyOtp( String otpToken, String clientUid, String userName, int urtId, MessageContext context )
	{
		ResponseMessageModel response = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		if ( OtpGenerator.verifyTimeBased( userName, clientUid, otpToken, TimeIntervalMode.TENMIN ) )
		{
			Session session = null;
			Transaction txn = null;
			try
			{
				session = HibernateSession.openSession();
				txn = session.beginTransaction();
				UserTbl userTbl = createUserTbl( urtId );
				session.save( userTbl );
				UserApplicationAccess userApplicationAccess = HibernateSession.createObject( UserApplicationAccess.class );
				userApplicationAccess.setApplicationTbl( HibernateSession.get( ApplicationTbl.class, 1 ) );
				userApplicationAccess.setUserTbl( userTbl );
				userApplicationAccess.setUaaAccessFl( true );
				userApplicationAccess.setUaaStartupHelper( 1 );
				session.save( userApplicationAccess );
				UserPassword userPassword = HibernateSession.createObject( UserPassword.class );
				userPassword.setUpwPassword( userTbl.getUsrPassword() );
				userPassword.setChangingUserTbl( userTbl );
				userPassword.setEffectedUserTbl( userTbl );
				userPassword.setUpwDttm( new DateTime() );
				session.save( userTbl );
				session.save( userPassword );
				txn.commit();

			}
			catch ( Exception e )
			{
				if ( txn != null )
					txn.rollback();
			}
			finally
			{
				HibernateSession.closeSession( session );
			}

			response.setMessage( "User Register successfully.send the otp to respective mobile no please verify it." );
			response.setStatusCode( Status.ACCEPTED.getStatusCode() );
		}
		else
		{
			response.setMessage( "Invalid otp .Please try again." );
			response.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
		}

		return response;
	}

	private UserTbl createUserTbl( int urtId )
	{
		UserRegistrationTbl userRegistrationTbl = HibernateSession.get( UserRegistrationTbl.class, urtId );
		UserTbl userTbl = HibernateSession.createObject( UserTbl.class );
		userTbl.setUsrName( userRegistrationTbl.getUrtName() );
		userTbl.setUsrPassword( userRegistrationTbl.getUrtPassword() );
		userTbl.setUsrForename( userRegistrationTbl.getUrtForename() );
		userTbl.setUsrSurname( userRegistrationTbl.getUrtSurname() );
		userTbl.setUsrEmailAddress( userRegistrationTbl.getUrtEmailAddress() );
		userTbl.setUsrMobNo( userRegistrationTbl.getUrtMobNo() );
		userTbl.setUsrDescription( userRegistrationTbl.getUrtDescription() );
		userTbl.setCtyId( userRegistrationTbl.getCtyId());
		userTbl.setUsrExpiryDttm( DateHelper.getMaximumDate() );
		userTbl.setUsrMobVerifyFl( true );
		userTbl.setUsrCurrEncryptFl( true );
		return userTbl;
	}

	public ResponseMessageModel validateMobileNoExist( String mobNo, MessageContext context )
	{
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.NOT_ACCEPTABLE.getStatusCode() );
		Session session = null;
		try
		{
			session = HibernateSession.openSession();
			List<UserTbl> userTbl = UserDAO.getUserByMobileNo( session, mobNo );
			if ( userTbl.isEmpty() )
			{
				responseMessageModel.setMessage( Status.ACCEPTED.getStatusCode(), "Mobile number validation sucess" );
			}
			else
			{
				responseMessageModel.setMessage( Status.NOT_ACCEPTABLE.getStatusCode(), "Mobile number already registered with us." );
			}
		}
		catch ( Exception e )
		{
			responseMessageModel.setMessage( Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed to fetch information,Internal server down" );
		}
		finally
		{
			if ( session != null )
				session.close();
		}
		return responseMessageModel;
	}

	public ResponseMessageModel validateEmailIdExist( String emailId, MessageContext context )
	{
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.NOT_ACCEPTABLE.getStatusCode() );
		Session session = null;
		try
		{
			session = HibernateSession.openSession();
			List<UserTbl> userTbl = UserDAO.getUserByMobileNo( session, emailId );
			if ( userTbl.isEmpty() )
			{
				responseMessageModel.setMessage( Status.ACCEPTED.getStatusCode(), "Email Id validation sucess" );
			}
			else
			{
				responseMessageModel.setMessage( Status.NOT_ACCEPTABLE.getStatusCode(), "Email Id already registered with us." );
			}
		}
		catch ( Exception e )
		{
			responseMessageModel.setMessage( Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed to fetch information,Internal server down" );
		}
		finally
		{
			if ( session != null )
				session.close();
		}
		return responseMessageModel;
	}

	public ResponseMessageModel activateEmail( EmailAccessModel emailModel, MessageContext context )
	{
		ResponseMessageModel responseMessageModel = new ResponseMessageModel( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
		Session session = null;
		Transaction txn = null;
		try
		{
			session = HibernateSession.openSession();
			List<EmailValidationToken> emailValidationTokens = EmailValidationTokenDAO.getEmailValidationTokenByEmailId( emailModel.getEmailId(), emailModel.getEvtToken(), session );
			if ( emailValidationTokens.isEmpty() )
			{
				responseMessageModel.setMessage( Status.NOT_ACCEPTABLE.getStatusCode(), "Bad Request !!! ,Invalid email id." );
				return responseMessageModel;
			}
			EmailValidationToken emailValidationToken = emailValidationTokens.get( 0 );

			if ( emailValidationToken.getEvtAccessKey().equals( SecurityHelper.hashPassword( emailModel.getEvtAccessKey() ) ) )
				responseMessageModel.setMessage( Status.NOT_ACCEPTABLE.getStatusCode(), "Invalid Access key.Please provide correct access key." );
			else if ( emailValidationToken.getEvtExpireDttm().isBefore( new DateTime() ) )
				responseMessageModel.setMessage( Status.NOT_ACCEPTABLE.getStatusCode(), "Link Expired.Please regenerate your activation link using ncash app." );
			else
			{
				txn = session.beginTransaction();
				UserTbl userTbl = emailValidationToken.getUserTbl();
				userTbl.setUsrEmailVerifyFl( true );
				session.update( userTbl );
				txn.commit();
				responseMessageModel.setMessage( Status.ACCEPTED.getStatusCode(), "Thank you for validating your email address." );
			}

		}
		catch ( Exception e )
		{
			if ( txn != null )
				txn.rollback();
			responseMessageModel.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
			responseMessageModel.setMessage( "Internal Server Error" );
		}
		finally
		{
			if ( session != null )
				session.close();
		}
		return responseMessageModel;
	}

	public ResponseMessageModel forgotPassword( String clientUid, String userName, MessageContext context )
	{

		ResponseMessageModel response = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		Session session = null;
		try
		{
			session = HibernateSession.openSession();
			List<UserTbl> userTbls = UserDAO.getUserByName( session, userName );
			if ( userTbls.isEmpty() )
			{
				response.setMessage( "User is not registered . Please signup to use NCASH services." );
				response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				return response;
			}
			else
			{
				UserTbl userTbl = userTbls.get( 0 );
				String otpKey = OtpGenerator.generateTimeBasedOtp( userName, clientUid, TimeIntervalMode.TENMIN );
				String otpMessage = " your NCASH one time password for changing password is " + otpKey;
				if ( NCashServerProperties.isDevMode() )
					System.out.println( otpMessage );
				else
					SmsSenderUtil.sendOtpSms( userTbl.getUsrMobNo(), userTbl.getCityTbl().getState().getCountryTbl().getCtrDialCode(), otpMessage, "NCAOTP" );
				response.setMessage( "Sent the otp to respective mobile no please verify it." );
				response.setStatusCode( Status.ACCEPTED.getStatusCode() );
			}
		}
		catch ( Exception e )
		{
			response.setStatusCode( Status.INTERNAL_SERVER_ERROR.getStatusCode() );
			response.setMessage( "Internal Server Error" );
		}
		finally
		{
			if ( session != null )
				session.close();
		}
		return response;
	}

	public ResponseMessageModel verifyAndChangePasswordForForgotPassword( String otpToken, String clientUid, String userName, String password, MessageContext context )
	{
		ResponseMessageModel response = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		Session session = null;
		try
		{
			session = HibernateSession.openSession();
			UserTbl userTbl = validateUserExist( session, response, userName );
			if ( userTbl == null )
			{
				response.setMessage( "User is not register . Please signup to use NCASH services." );
				response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				return response;
			}
			if ( !OtpGenerator.verifyTimeBased( userName, clientUid, otpToken, TimeIntervalMode.TENMIN ) )
			{
				response.setMessage( "Invaild otp or otp expired ." );
				response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				return response;
			}
			response = validateAndSavePassword( session, response, userTbl, password );
		}
		catch ( Exception e )
		{
			response.setMessage( "Internal Server Error" );
		}
		return response;
	}

	public ResponseMessageModel verifyAndChangePassword( String userName, String oldPassword, String password, MessageContext context )
	{
		ResponseMessageModel response = new ResponseMessageModel( Status.EXPECTATION_FAILED.getStatusCode() );
		Session session = null;
		try
		{
			session = HibernateSession.openSession();
			UserTbl userTbl = validateUserExist( session, response, userName );
			if ( userTbl == null )
			{
				response.setMessage( "User is not register . Please signup to use NCASH services." );
				response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				return response;
			}
			if ( !oldPassword.equals( userTbl.getUsrPassword() ) )
			{
				response.setMessage( "Invaild old password . Please re-enter your old password." );
				response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				return response;
			}
			response = validateAndSavePassword( session, response, userTbl, password );
		}
		catch ( Exception e )
		{
			response.setMessage( "Internal Server Error" );
		}
		return response;
	}

	private UserTbl validateUserExist( Session session, ResponseMessageModel response, String userName )
	{
		List<UserTbl> userTbls = UserDAO.getUserByName( session, userName );
		if ( userTbls.isEmpty() )
		{
			return null;
		}
		UserTbl usrTbl = userTbls.get( 0 );
		return usrTbl;

	}

	private ResponseMessageModel validateAndSavePassword( Session session, ResponseMessageModel response, UserTbl usrTbl, String password )
	{
		List<String> reason = new ArrayList<String>();
		String hashPassword = SecurityHelper.hashPassword( password );
		usrTbl.setUsrPassword( hashPassword );

		if ( hashPassword.equals( usrTbl.getUsrPassword() ) )
		{
			response.setMessage( "User is can't be same as last 3 password ." );
			response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
		}
		else if ( password.equals( usrTbl.getUsrName() ) || password.equals( usrTbl.getUsrSurname() ) || password.equals( usrTbl.getUsrForename() ) )
		{
			response.setMessage( "User is can't be same as username or forname or surname ." );
			response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
		}
		else if ( passwordValidation != null && !passwordValidation.isPasswordValid( session, password, reason, usrTbl ) )
		{
			response.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
			response.setMessage( reason.get( 0 ) );
			if ( reason.size() > 1 )
			{
				response.addError( "password", reason.get( 1 ) );
			}
		}

		UserPassword usrPassword = new UserPassword( true );
		usrPassword.setUpwPassword( hashPassword );
		usrPassword.setEffectedUserTbl( usrTbl );
		usrPassword.setChangingUserTbl( usrTbl );
		usrPassword.setUpwDttm( new DateTime() );
		usrPassword.setUpwResetFl( true );
		usrPassword.setPartitionId( usrTbl.getPartitionId() );

		session.saveOrUpdate( usrTbl );
		session.saveOrUpdate( usrPassword );
		return response;

	}

	public PasswordValidationInterface getPasswordValidation()
	{
		return passwordValidation;
	}

	public void setPasswordValidation( PasswordValidationInterface passwordValidation )
	{
		this.passwordValidation = passwordValidation;
	}
}
