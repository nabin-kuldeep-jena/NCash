package com.asjngroup.ncash.security.login.service;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.asjngroup.ncash.common.database.hibernate.references.ApplicationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.ConnectedUser;
import com.asjngroup.ncash.common.database.hibernate.references.UserApplicationAccess;
import com.asjngroup.ncash.common.database.hibernate.references.UserLoginInfo;
import com.asjngroup.ncash.common.database.hibernate.references.UserPassword;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.models.UserSession;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.models.UserDetailsModel;
import com.asjngroup.ncash.common.util.NCashConstant;
import com.asjngroup.ncash.common.util.ResourceManager;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;
import com.asjngroup.ncash.framework.security.component.PasswordValidationInterface;
import com.asjngroup.ncash.framework.security.helper.SecurityHelper;
import com.asjngroup.ncash.framework.security.model.LoginAttempt;
import com.asjngroup.ncash.security.constant.AuthenticateConfigConstant;
import com.asjngroup.ncash.security.service.models.UserTblModel;

@Component( value = "authenticationService" )
public class NCashAuthenticationServiceImpl implements AuthenticationService
{
	@Autowired( required = true )
	@Qualifier( "passwordValidation" )
	PasswordValidationInterface passwordValidation;

	private static final int INDEX = 11;

	private static final int TotalNoMsg = 10;

	private boolean allowForcedLogin = false;

	public static ArrayList<String> getLoadParameters()
	{
		Properties prop = new Properties();

		URL logURL = AuthenticationService.class.getResource( AuthenticateConfigConstant.PAGE_CONFIG_PROP_FILE_NAME );
		if ( logURL == null )
		{
			throw new NCashRuntimeException( "Failed to locate properties file - " + AuthenticateConfigConstant.PAGE_CONFIG_PROP_FILE_NAME );
		}

		InputStream openStream = null;

		try
		{
			openStream = logURL.openStream();
			InputStreamReader in = new InputStreamReader( openStream, Charset.forName( NCashConstant.DEFAULT_CHARSET ) );
			prop.load( in );
		}
		catch ( FileNotFoundException e )
		{
			throw new NCashRuntimeException( "Failed to locate properties file - " + AuthenticateConfigConstant.PAGE_CONFIG_PROP_FILE_NAME );
		}
		catch ( IOException e )
		{
			throw new NCashRuntimeException( "Failed to read properties file - " + AuthenticateConfigConstant.PAGE_CONFIG_PROP_FILE_NAME );
		}
		finally
		{
			if ( null != openStream )
			{
				try
				{
					openStream.close();
				}
				catch ( Exception e )
				{
					throw new NCashRuntimeException( "Failed to close properties file - " + AuthenticateConfigConstant.PAGE_CONFIG_PROP_FILE_NAME );
				}
			}
		}

		ArrayList<String> listStr = new ArrayList<String>();
		listStr.add( prop.getProperty( AuthenticateConfigConstant.APPLICATION_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.USER_NAME_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.PASS_WORD_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.LOGIN_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.ORIENTATION_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.LOCALE_INFO_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.COPYRIGHT_MESS_KEY ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.REMEMBER_PASSWORD ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.GROUP_SEPERATOR ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.DECIMAL_SEPERATOR ) );
		listStr.add( prop.getProperty( AuthenticateConfigConstant.WINDOW_TITLE ) );

		for ( int i = 1; i <= TotalNoMsg; i++ )
		{
			listStr.add( prop.getProperty( AuthenticateConfigConstant.SPARK_MESSAGE + i ) );
		}

		return listStr;
	}

	@SuppressWarnings( "unchecked" )
	public static String[] getApplications()
	{
		Session session = HibernateSession.openSession();

		List<String> applictions = session.createQuery( "Select app.appName From ApplicationTbl app where app.appProductName <> 'SmallDevices'" ).list();

		session.close();

		String[] apps = new String[applictions.size()];
		int i = 0;
		for ( String name : applictions )
			apps[i++] = name;

		return apps;
	}

	public boolean doLoginAction( ResponseMessageModel errorResponse, List<UserTblModel> userTbls, UserDetailsModel userDetailsModel )
	{
		Session hibernateSession = null;
		UserTbl userTable = null;
		Transaction txn = null;
		try
		{
			userDetailsModel.setHashedPassword( SecurityHelper.hashPassword( userDetailsModel.getUnHashedPassword(), "SHA-512" ) );
			hibernateSession = HibernateSession.openSession();
			txn = hibernateSession.beginTransaction();
			userTable = getUserTbl( hibernateSession, userDetailsModel.getUserName() );
			LoginAttempt loginAttempt = new LoginAttempt( userTable, userDetailsModel.getUnHashedPassword(), true );
			userDetailsModel.setLoginAttempt( loginAttempt );
			loginAttempt.setClientIpAddress( userDetailsModel.getClientIpAddress() );
			loginAttempt.setClientHostName( userDetailsModel.getClientHostName() );
			loginAttempt.setGivenUserName( userDetailsModel.getUserName() );

			if ( validateUser( hibernateSession, userTable, errorResponse, userDetailsModel.getLocale(), userDetailsModel ) )
			{
				saveConnectedUserInfo( hibernateSession, userDetailsModel );
				UserTblModel userTblModel = buildUserTblResponse( userTable );
				userTbls.add( userTblModel );
				txn.commit();
				return true;
			}
			txn.commit();
			return false;
		} // end of try
		catch ( HibernateException hibernateException )
		{
			if ( txn != null )
				txn.rollback();
			return false;
		} // catch ( HibernateException hibernateException )
		catch ( Exception generalException )
		{
			if ( txn != null )
				txn.rollback();
			return false;
		} // catch ( Exception generalException )
		finally
		{
			if ( hibernateSession != null )
				hibernateSession.close();
		} // end of finally block
	}// public boolean doLogin( ReturnString returnString )

	private UserTblModel buildUserTblResponse( UserTbl userTable )
	{
		UserTblModel userTblModel = new UserTblModel();
		userTblModel.setId( userTable.getId() );
		userTblModel.setUsrForename( userTable.getUsrForename() );
		userTblModel.setUsrSurname( userTable.getUsrSurname() );
		userTblModel.setUsrMobNo( userTable.getUsrMobNo() );
		userTblModel.setUsrEmailAddress( userTable.getUsrEmailAddress() );
		//userTblModel.setUsrCountryNoCode( userTable.getCountryTbl().getCtrDialCode() );
		//CityTbl preferredOpt = fetchPreferredStateAndCity( userTable.getUsrId() );
		userTblModel.setUsrCtyId( userTable.getCtyId() );
		userTblModel.setUsrSteId( userTable.getCityTbl().getSteId() );
		return userTblModel;
	}

	private boolean validateUser( Session session, UserTbl userTbl, ResponseMessageModel returnString, String locale, UserDetailsModel userDetailsModel )
	{
		LoginAttempt loginAttempt = userDetailsModel.getLoginAttempt();
		try
		{
			if ( userTbl != null && userTbl.getUsrCurrEncryptFl() )
				loginAttempt.setHashedPassword( SecurityHelper.hashPassword( userDetailsModel.getUnHashedPassword(), "SHA-512" ) );
			else
				loginAttempt.setHashedPassword( userDetailsModel.getUnHashedPassword() );

			if ( validateUserName( session, loginAttempt, loginAttempt.getGivenUserName(), locale, userDetailsModel ) )
			{
				if ( passwordValidation.validateLoginAttempt( session, loginAttempt, userTbl ) /*&& validateAuthorization( session, loginAttempt, loginAttempt.getGivenUserName(), userDetailsModel.getAppName() )*/ )
				{
					loginAttempt.setSuccess( true );
					doOnLogin( session, userTbl, locale, userDetailsModel );
				}
				else
				{
					attemptForceLogin( session, userTbl, userDetailsModel );
				}
			}
			if ( returnString.getMessage() == null || returnString.getMessage() == "" )
			{
				if ( loginAttempt.getMessage() != null || loginAttempt.getMessage() != "" )
				{
					returnString.setMessage( loginAttempt.getMessage() );
					returnString.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );

					if ( loginAttempt.getMessageParameter() == null && userTbl != null && userTbl.getUsrDisabledFl() )
					{
						String sqlQueryBuffer = "select count(*) from UserLoginInfo uli where uli.userTbl.id = " + ":usrId and uli.ulgSuccessFl = false ";

						Long numLoginAttempts = ( Long ) session.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).list().get( 0 );
						loginAttempt.setMessageParameter( new String[]
						{ numLoginAttempts.toString() } );
					}
					returnString.addError( "ALERT", StringUtil.create( loginAttempt.getMessage(), loginAttempt.getMessageParameter() ) );
				}
				else
				{
					loginAttempt.setMessage( "" );
					returnString.setMessage( "" );
				}
			}
		}

		catch ( Exception generalException )
		{
			generalException.printStackTrace();
			if ( returnString.getMessage() == null || returnString.getMessage() == "" )
			{
				returnString.setMessage( StringUtil.create( "Invalid user name or password" ) );
				returnString.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
			}

			return false;
		} // catch ( Exception generalException )

		return loginAttempt.isSuccess();
	}

	private void attemptForceLogin( Session session, UserTbl userTbl, UserDetailsModel userDetailsModel )
	{
		try
		{
			if ( allowForcedLogin )
			{
				String query = "select a.cusSid from ConnectedUser a,  UserLogin b " + "where a.ulgId = b.ulgId and b.usrId = :userId";

				List<String> sessions = HibernateSession.query( query, "userId", userDetailsModel.getLoginAttempt().getUserTbl().getUsrId() );

				if ( !sessions.isEmpty() )
				{
					HttpSession webSession = null;//( HttpSession ) InitialiseHelper.getSessionMap().get( sessions.get( 0 ) );

					if ( webSession != null )
					{
						webSession.invalidate();
					}
				}

				doOnLogin( session, userTbl, userDetailsModel.getLocale(), userDetailsModel );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			System.out.println( "Exception in Force login" );
		}
	}

	private void doOnLogin( Session session, UserTbl userTbl, String locale, UserDetailsModel userDetailsModel )
	{
		System.out.println( "in doOnLogin start : session cleanup" );
		doPostLoginAction( session, userTbl, locale, userDetailsModel );
		UserSession userSession = userDetailsModel.getUserSession();
		LoginAttempt loginAttempt = userDetailsModel.getLoginAttempt();
		userSession.setChangePasswordApplicable( loginAttempt.isChangePasswordApplicable() );
		userSession.setMessageText( loginAttempt.getMessage() );
		userSession.setLastLogonText( loginAttempt.getLogonMessageText() );
		userSession.setMessageParameter( loginAttempt.getMessageParameter() );

		System.out.println( "in doOnLogin End : session cleanup" );
	}

	public boolean doSSOLogin( ResponseMessageModel responseMessageModel, List<UserTblModel> userTbls, boolean isNcash, UserDetailsModel userDetailsModel )
	{
		boolean isSuccess = false;

		Session session = HibernateSession.openSession();
		Transaction txn = session.beginTransaction();

		UserTbl userTbl = getUserTbl( session, userDetailsModel.getUserName() );

		if ( userTbl != null )
			userDetailsModel.setUserName( userTbl.getUsrName() );

		// validate the login
		if ( userTbl == null || !userTbl.getUsrName().equals( userDetailsModel.getUserName() ) )
		{
			// user not valid so fail login
			if ( userDetailsModel.getUserName() == null || "".equals( userDetailsModel.getUserName() ) )
			{
				responseMessageModel.setMessage( StringUtil.create( "Invalid Username or Password" ) );
				isSuccess = false;
			}
		}
		else if ( userTbl.getUsrDisabledFl() )
		{
			isSuccess = false;
		}
		else
		{
			if ( userTbl.getDeleteFl() )
			{
				responseMessageModel.setMessage( StringUtil.create( "Invalid Username or Password" ) );
				return false;
			}
			try
			{
				LoginAttempt loginAttempt = new LoginAttempt( userTbl, userTbl.getUsrPassword(), true );
				userDetailsModel.setLoginAttempt( loginAttempt );
				loginAttempt.setClientIpAddress( userDetailsModel.getClientIpAddress() );
				loginAttempt.setClientHostName( userDetailsModel.getClientHostName() );
				loginAttempt.setGivenUserName( userDetailsModel.getUserName() );
				boolean valid = true;

				UserSession userSession = userDetailsModel.getUserSession();
				Boolean isDatabaseLogin = Boolean.parseBoolean( System.getProperty( "DatabaseLogin" ) );
				String locale = userDetailsModel.getLocale();
				if ( isNcash )
				{
					if ( isDatabaseLogin )
						valid = validateUser( session, userTbl, responseMessageModel, locale, userDetailsModel );
					else
					{
						valid = validateUserExpiryDttmAndAllowedMachine( session, responseMessageModel, userDetailsModel );
					}
				}
				else
				{
					if ( userSession == null )
						userSession = createUserSession( session, getUserTbl( session, userDetailsModel.getUserName() ), locale, userDetailsModel, userDetailsModel.getConnectedUser().getUserLoginInfo());
					userSession.setChangePasswordApplicable( false );
					userDetailsModel.setUserSession( userSession );
				}
				if ( valid )
				{
					userSession.setSSO( true );
					userSession.setLogoutUrl( userDetailsModel.getLogoutUrl() );
					userSession.setSessionId( userDetailsModel.getSessionId() );
					passwordValidation.doDatabaseBookKeeping( session, loginAttempt, true, "Successful login" );
					saveConnectedUserInfo( session, userDetailsModel );
					UserTblModel userTblModel = buildUserTblResponse( userTbl );
					userTbls.add( userTblModel );
					isSuccess = true;
				}
			}
			catch ( Exception e )
			{
				if ( txn != null )
					txn.rollback();
				return false;
			}
			//			doPostLoginChores( session, userTbl, locale );
			//			saveConnectedUserInfo( session );
			//			isSuccess = true;
		}
		if ( txn != null )
			txn.commit();
		session.close();
		return isSuccess;

	}

	private boolean validateUserName( Session hibernateSession, LoginAttempt userLoginAttempt, String givenUserName, String locale, UserDetailsModel userDetailsModel )
	{
		UserTbl userTable = null;

		try
		{
			LoginAttempt loginAttempt = userDetailsModel.getLoginAttempt();
			userTable = userLoginAttempt.getUserTbl();
			if ( givenUserName == null || "".equals( givenUserName ) || userDetailsModel.getHashedPassword() == null || "".equals( userDetailsModel.getHashedPassword() ) )
			{
				userLoginAttempt.failedLogin( StringUtil.create( "Invalid Username or Password" ) );
				userLoginAttempt.setMessageId( AuthenticateConfigConstant.INVALID_USERNAME_OR_PASSWORD );
				return false;
			}
			else if ( userTable == null )
			{
				userLoginAttempt.failedLogin( StringUtil.create( "Invalid Username or Password" ) ); // set
				userLoginAttempt.setMessageId( AuthenticateConfigConstant.INVALID_USERNAME_OR_PASSWORD );
				passwordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "Failed login - Invalid user name '" + givenUserName + "'" ) );
				return false;
			}
			else
			{
				if ( userTable.getUsrDisabledFl() )
				{
					userLoginAttempt.failedLogin( StringUtil.create( ResourceManager.getI18NString( "Common", "Login failed for disabled username '%1'" ), loginAttempt.getGivenUserName() ) );
					userLoginAttempt.setMessageId( AuthenticateConfigConstant.USER_DISABLED );
					passwordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "Failed login - Disable user '" + givenUserName + "'" ) );
					return false;
				}
				return validateUserExpiryDttmAndAllowedMachine( hibernateSession, loginAttempt );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean validateUserExpiryDttmAndAllowedMachine( Session hibernateSession, ResponseMessageModel responseMessageModel, UserDetailsModel userDetailsModel )
	{
		boolean valid = validateUserExpiryDttmAndAllowedMachine( hibernateSession, userDetailsModel.getLoginAttempt() );
		if ( responseMessageModel.getMessage() == null || responseMessageModel.getMessage() == "" )
		{
			LoginAttempt loginAttempt = userDetailsModel.getLoginAttempt();
			if ( loginAttempt.getMessage() != null || loginAttempt.getMessage() != "" )
			{
				responseMessageModel.setMessage( loginAttempt.getMessage() );
				responseMessageModel.setStatusCode( Status.NOT_ACCEPTABLE.getStatusCode() );
				responseMessageModel.addError( "ALERT", StringUtil.create( loginAttempt.getMessage(), loginAttempt.getMessageParameter() ) );
			}
			else
			{
				loginAttempt.setMessage( "" );
				responseMessageModel.setMessage( "" );
			}
		}
		UserSession userSession = userDetailsModel.getUserSession();
		if ( userSession == null )
			userSession = createUserSession( hibernateSession, getUserTbl( hibernateSession, userDetailsModel.getUserName() ), responseMessageModel.getMessage(), userDetailsModel ,userDetailsModel.getConnectedUser().getUserLoginInfo());
		userSession.setChangePasswordApplicable( false );
		userDetailsModel.setUserSession( userSession );
		return valid;
	}

	public boolean validateUserExpiryDttmAndAllowedMachine( Session hibernateSession, LoginAttempt loginAttempt )
	{
		UserTbl userTable = loginAttempt.getUserTbl();
		if ( userTable.getUsrExpiryDttm() != null )
		{
			DateTime sysDate = new DateTime( System.currentTimeMillis() );
			if ( sysDate.isAfter( userTable.getUsrExpiryDttm() ) )
			{
				loginAttempt.failedLogin( StringUtil.create( "User Account Expired" ) ); //set the user to root
				loginAttempt.setMessageId( AuthenticateConfigConstant.USER_EXPIRED );
				passwordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "Failed login - Expired user '" + loginAttempt.getGivenUserName() + "'" ) );
				return false;
			}
		}
		/*if ( StringHelper.isNotEmpty( userTable.getUsrAllowedMachines() ) )
		{
			if ( !( userTable.getUsrAllowedMachines().contains( loginAttempt.getClientIpAddress() ) || userTable.getUsrAllowedMachines().contains( loginAttempt.getClientHostName() ) ) )
			{
				loginAttempt.failedLogin( StringUtil.create( ResourceManager.getI18NString( "Common", "Access from %1 denied" ), loginAttempt.getClientIpAddress() ) );
				loginAttempt.setMessageId( AuthenticateConfigConstant.MACHINE_BOUND );
		
				loginAttempt.setMessageParameter( new String[]
				{ loginAttempt.getClientIpAddress() } );
				StandardUserPasswordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "Failed login -  User  '" + loginAttempt.getGivenUserName() + "' can't access from '" + loginAttempt.getClientIpAddress() + "' ip/hostname." ) );
				return false;
			}
		
		}*/

		return true;
	}

	public boolean validateAuthorization( Session hibernateSession, LoginAttempt loginAttempt, String givenUserName, String appName )
	{
		UserTbl userTable = null;

		try
		{
			userTable = loginAttempt.getUserTbl();

			if ( userTable == null )
				return false;

			if ( !isHavingApplicationAccessPermission( appName, givenUserName, loginAttempt ) )
			{
				loginAttempt.failedLogin( StringUtil.create( "You do not have privilege to access the application '%1' ", appName ) );
				loginAttempt.setMessageId( AuthenticateConfigConstant.NO_PRIVLEGE_TO_APPLICATION );
				loginAttempt.setMessageParameter( new String[]
				{ appName } );
				passwordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "User does not have privilege to access the application '" + appName + "'" ) );
				return false;
			}

			/*	else if ( userTable.getUserRolePartitions().size() == 0 )
				{
					userLoginAttempt1.failedLogin( StringUtil.create( "User '" + givenUserName + "' does not have any Role access" ) );
					userLoginAttempt1.setMessageId( AuthenticateConfigConstant.NO_ROLE_ACCESS );
					userLoginAttempt1.setMessageParameter( new String[]
					{ givenUserName } );
					passwordValidation.doDatabaseBookKeeping( hibernateSession, loginAttempt, false, ( "User '" + givenUserName + "' does not have any partition / Role access" ) );
					return false;
				}*/

		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean isHavingApplicationAccessPermission( String applicationName, String givenUserName, LoginAttempt loginAttempt )
	{
		UserTbl userTable = null;

		try
		{
			userTable = loginAttempt.getUserTbl();
			Collection<UserApplicationAccess> appUserCol = userTable.getUserApplicationAccesses();
			for ( UserApplicationAccess applicationUser : appUserCol )
			{
				if ( applicationUser.getUserTbl().getUsrName().equalsIgnoreCase( givenUserName ) && applicationUser.getApplicationTbl().getAppName().equals( applicationName ) )
					if ( applicationUser.getUaaAccessFl() )
						return true;
				return false;
			}

		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return false;

	}

	@SuppressWarnings( "unchecked" )
	private UserTbl getUserTbl( Session session, String userName )
	{
		List<UserTbl> userTbls = session.createQuery( "from UserTbl ust where ust.usrName = :usrName " ).setParameter( "usrName", userName ).list();
		return ( userTbls.size() != 1 ? null : userTbls.get( 0 ) );
	}

	private void doPostLoginAction( Session session, UserTbl userTbl, String locale, UserDetailsModel userDetailsModel )
	{
		try
		{
			List<UserLoginInfo> userLoginList = session.createQuery( "from UserLoginInfo uli where uli.uliId = :uliId" ).setParameter( "uliId", userDetailsModel.getLoginAttempt().getUsrLoginId() ).list();
			if ( !userLoginList.isEmpty() )
			{
				userDetailsModel.setConnectedUser( setConnectedUserInfo( userLoginList.get( 0 ), userDetailsModel.getLoginAttempt(), userDetailsModel.getSessionId() ) );
			}

			UserSession userSession = createUserSession( session, userTbl, locale, userDetailsModel, userLoginList.get( 0 ) );
			userDetailsModel.setUserSession( userSession );

			if ( userTbl.getUsrCurrEncryptFl() )
			{
				updatePasswordToNewEncryption( session, userTbl, userDetailsModel.getUnHashedPassword(), userDetailsModel.getHashedPassword() );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void updatePasswordToNewEncryption( Session session, UserTbl userTbl, String unHashedPassword, String hashedPassword )
	{
		String tmphasedPassword = SecurityHelper.hashPassword( unHashedPassword, "SHA-512" );
		if ( !tmphasedPassword.equals( hashedPassword ) )
		{
			hashedPassword = tmphasedPassword;
			userTbl.setUsrPassword( hashedPassword );
			userTbl.setUsrCurrEncryptFl( true );

			UserPassword userPassword;
			List<UserPassword> userPasswords = HibernateSession.query( "from UserPassword upw  where upw.upwEffectedUsrId = :usrId order by upw.upwId desc", new String[]
			{ "usrId" }, new Object[]
			{ userTbl.getUsrId() }, 1 );
			if ( userPasswords.isEmpty() )
			{
				userPassword = HibernateSession.createObject( UserPassword.class );
				userPassword.setUpwChangingUsrId( userTbl.getUsrId() );
				userPassword.setUpwEffectedUsrId( userTbl.getUsrId() );
				userPassword.setUpwDttm( new DateTime() );
			}
			else
			{
				userPassword = userPasswords.get( 0 );
			}

			userPassword.setUpwPassword( hashedPassword );
			session.saveOrUpdate( userPassword );
		}

		session.saveOrUpdate( userTbl );
	}

	protected ConnectedUser setConnectedUserInfo( UserLoginInfo userLogin, LoginAttempt loginAttempt, String sessionId )
	{
		ConnectedUser connectedUser = HibernateSession.createObject( ConnectedUser.class );
		connectedUser.setUserLoginInfo( userLogin );
		connectedUser.setUliId( loginAttempt.getUsrLoginId() );
		connectedUser.setCusSid( sessionId );
		connectedUser.setPartitionId( userLogin.getPartitionId() );
		return connectedUser;
	}

	private void saveConnectedUserInfo( Session session, UserDetailsModel userDetailsModel )
	{
		ConnectedUser connectedUser = userDetailsModel.getConnectedUser();
		if ( connectedUser == null )
			return;

		session.save( connectedUser );
	}

	private UserSession createUserSession( Session session, UserTbl userTbl, String locale, UserDetailsModel userDetailsModel, UserLoginInfo userLoginInfo )
	{
		UserSession newUserSession = new UserSession();
		userDetailsModel.setUserSession( newUserSession );
		newUserSession.setRemoteAddr( userDetailsModel.getClientIpAddress() );
		newUserSession.setRemoteHost( userDetailsModel.getClientHostName() );
		newUserSession.setLocale( locale );
		newUserSession.setUserTbl( userTbl );
		ApplicationTbl applicationTbl = null;
		if ( !StringHelper.isEmpty( userDetailsModel.getAppName() ) )
			applicationTbl = ( ApplicationTbl ) session.createQuery( "from ApplicationTbl app where app.appName = :appName" ).setParameter( "appName", userDetailsModel.getAppName() ).list().get( 0 );
		newUserSession.setApplicationTbl( applicationTbl );
		if ( locale != null )
			newUserSession.setLocale( locale );
		if ( userDetailsModel.getOrientation() != null )
			newUserSession.setRTL( !AuthenticateConfigConstant.ORIENTATIN_LTR.equals( userDetailsModel.getOrientation() ) );

		newUserSession.setGroupSeperator( userDetailsModel.getGroupSeperator() );
		newUserSession.setDecimalSeperator( userDetailsModel.getDecimalSeperator() );
		newUserSession.setLoginhappened( true );
		return newUserSession;
	}

	//	private CityTbl fetchPreferredStateAndCity( int usrId )
	//	{
	//		UserExtraDetail userDetail = HibernateSession.queryExpectOneRow( "from UserExtraDetail ued where ued.usrId = :usrId", "usrId", usrId );
	//		if ( userDetail != null )
	//			return userDetail.getCityTbl();
	//
	//		return null;
	//	}

	public boolean isAllowForcedLogin()
	{
		return allowForcedLogin;
	}

	public void setAllowForcedLogin( boolean allowForcedLogin )
	{
		this.allowForcedLogin = allowForcedLogin;
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
