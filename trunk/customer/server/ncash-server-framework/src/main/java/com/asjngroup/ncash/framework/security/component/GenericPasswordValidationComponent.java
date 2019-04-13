package com.asjngroup.ncash.framework.security.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.BinaryHelper;
import com.asjngroup.ncash.common.database.hibernate.references.ApplicationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.UserLoginInfo;
import com.asjngroup.ncash.common.database.hibernate.references.UserPassword;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.DateHelper;
import com.asjngroup.ncash.common.util.NCashConstant;
import com.asjngroup.ncash.common.util.ResourceManager;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;
import com.asjngroup.ncash.framework.security.helper.SecurityHelper;
import com.asjngroup.ncash.framework.security.model.LoginAttempt;
import com.asjngroup.ncash.security.constant.AuthenticateConfigConstant;

public class GenericPasswordValidationComponent implements PasswordValidationInterface
{
	private boolean skipServerAudit;

	private final String strInvalidPasswordMessageForDB = "Failed login - Invalid password";

	private final String strFailedLoginMessage = "Login failed for username '%1'";

	private final String strSuccessfulLogonForDB = "Successful login";

	private final String strPasswordExpiryMessage = "Your password will expire in '%1' days. You will receive this warning each time you login until your password is reset.";

	private final String strLoginAttemptsExpiredMessageForDB = "Failed login - User disabled after '%1' consecutive failed login attempts";

	private final String strUserPasswordExpired = "Your password has expired, if you had not received any warning earlier then you were inactive during last '%1' days of warnable period";

	protected final Log log = LogFactory.getLog( GenericPasswordValidationComponent.class );

	protected Map<String, String> mapPasswordValidationProperty = new HashMap<String, String>();

	protected Map<String, String> mapStringPasswordProperty = new HashMap<String, String>();

	protected boolean isStrongPasswordConfigured = false;

	private boolean sysLoginInformation = false;
	private boolean allowForcedLogin = false;
	private boolean isMultiLoginAlow = true;

	public GenericPasswordValidationComponent()
	{
		mapPasswordValidationProperty.put( "DisableAfterInactivePeriod", "90" );
		mapPasswordValidationProperty.put( "PasswordResetInterval", "90" );
		mapPasswordValidationProperty.put("PasswordWarningInterval","95");
		mapPasswordValidationProperty.put("DisableAfterFailedLoginAttempts" ,"10");
		mapPasswordValidationProperty.put("PasswordDefault","welcome");
	}

	@SuppressWarnings( "unchecked" )
	public boolean validateLoginAttempt(Session txnSession, LoginAttempt loginAttempt, UserTbl userTbl ) throws UserPasswordValidationException
	{
		DateTime dtLastSuccessfulLogin = null;
		DateTime dtLastUnSuccessfulLogin = null;
		String sqlQueryBuffer = null;

		try
		{
			if ( userTbl.getUsrDisabledFl() || userTbl.getDeleteFl() )
			{
				loginAttempt.failedLogin( StringUtil.create( ResourceManager.getI18NString( "Common", "Login failed for username %1" ), loginAttempt.getGivenUserName() ) );
				loginAttempt.setMessageId( AuthenticateConfigConstant.LOGIN_FAILED );
				loginAttempt.setMessageParameter( new String[]
				{ loginAttempt.getGivenUserName() } );
				String message = "Failed login - Disabled user '";
				if ( userTbl.getDeleteFl() )
				{
					message = StringUtil.create( "Failed login - Deleted user '" );
				}
				doDatabaseBookKeeping( txnSession, loginAttempt, false, message + loginAttempt.getGivenUserName() + "'" );

				return false;
			}

			sqlQueryBuffer = "select max( uli.uliDttm ) from UserLoginInfo uli where uli.userTbl.id = :usrId " + "and uli.uliSuccessFl = true and uli.uliDttm <= :now and uli_message not like 'User created%' ";

			List<DateTime> resultListDateTime = txnSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).setParameter( "now", new DateTime() ).list();

			if ( resultListDateTime.isEmpty() != true )
			{
				dtLastSuccessfulLogin = resultListDateTime.get( 0 );
			}

			resultListDateTime.clear();

			sqlQueryBuffer = "select max( uli.uliDttm ) from UserLoginInfo uli where uli.userTbl.id = :usrId " + "and uli.uliSuccessFl = false and uli.uliDttm <= :now and uli_message not like 'User created%' ";

			resultListDateTime = txnSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).setParameter( "now", new DateTime() ).list();

			if ( resultListDateTime.isEmpty() != true )
			{
				dtLastUnSuccessfulLogin = resultListDateTime.get( 0 );
			}

			if ( dtLastSuccessfulLogin != null )
			{
				log.info( StringUtil.create( "User '%1' last successfully logged in on %2", loginAttempt.getUserTbl().getUsrName(), DateHelper.yyyymmddhhmmssFormatter.print( dtLastSuccessfulLogin ) ) );

				// if our last successful login was not within this period then disable the user
				if ( dtLastSuccessfulLogin.isBefore( new DateTime().minusDays( Integer.parseInt( mapPasswordValidationProperty.get( "DisableAfterInactivePeriod" ) ) ) ) )
				{
					if ( NCashConstant.ADMINISTRATOR_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) || NCashConstant.DEFAULT_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) )
					{
						loginAttempt.setChangePasswordApplicable( true );
						doDatabaseBookKeeping( txnSession, loginAttempt, true, StringUtil.create( "User '%1' was inactive beyond '%2' day(s), password reset recommended.", loginAttempt.getGivenUserName(), Integer.parseInt( mapPasswordValidationProperty.get( "DisableAfterInactivePeriod" ) ) ) );
					}
					else
					{
						if ( disableUserLogin( loginAttempt ) == true )
						{
							DateTimeFormatter dtFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy HH:mm:ss" );

							loginAttempt.setDisableUserTbl( true );
							loginAttempt.failedLogin( StringUtil.create( "Login failed for username '%1'", loginAttempt.getGivenUserName() ) );
							loginAttempt.setMessageId( AuthenticateConfigConstant.LOGIN_FAILED );
							doDatabaseBookKeeping( txnSession, loginAttempt, false, StringUtil.create( "Failed login - User disabled due to no activity since '%1' ", dtFormatter.print( dtLastSuccessfulLogin ) ) );

							return false;
						}
					}
				}
			}

			if ( loginAttempt.isSuccess() != true )
			{
				return false;
			}

			//
			if ( loginAttempt.getUserTbl().getUsrPassword().equals( loginAttempt.getHashedPassword() ) )
			{
				if ( dtLastSuccessfulLogin != null && ( sysLoginInformation ) )
				{
					DateTimeFormatter dtFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy HH:mm:ss" );
					DateTimeFormatter dFormatter = DateTimeFormat.forPattern( "MM/dd/yyyy" );
					DateTimeFormatter tFormatter = DateTimeFormat.forPattern( "HH:mm:ss" );
					String strMessage = null;

					if ( dtLastUnSuccessfulLogin != null )
					{
						strMessage = StringUtil.create( "Last Successful Login: %1<br/>Last Unsuccessful Login: %2 %3", dtFormatter.print( dtLastSuccessfulLogin ), dFormatter.print( dtLastUnSuccessfulLogin ), tFormatter.print( dtLastUnSuccessfulLogin ) );

					}
					else
					{
						strMessage = StringUtil.create( "Last Successful Login: %1", dtFormatter.print( dtLastSuccessfulLogin ) );
					} // if ( dtLastUnSuccessfulLogin != null )

					loginAttempt.setLogonMessageText( strMessage );
				} //if ( dtLastSuccessfulLogin != null && ... )

				// Validate this login for multi user logon to the system, based on the system
				// property set for it
				//
				if ( checkForMultipleLogon( txnSession, loginAttempt ) != true )
				{
					if ( !allowForcedLogin )
					{
						loginAttempt.failedLogin( "User '" + loginAttempt.getGivenUserName() + "'" + " already logged in, if otherwise please contact your Administrator." );
						loginAttempt.setMessageId( AuthenticateConfigConstant.USER_ALREADY_LOGGED_IN );
						loginAttempt.setMessageParameter( new String[]
						{ loginAttempt.getGivenUserName() } );
						return loginAttempt.isSuccess();
					}
					else
					{
						doDatabaseBookKeeping( txnSession, loginAttempt, true, strSuccessfulLogonForDB );
						return false;
					}

				} //if ( checkForMultipleLogon( hibernateSession, loginAttempt) != true )

				// Validate the user for first time login into the system
				//
				if ( !doFirstTimeLoginValidation( txnSession, loginAttempt, dtLastSuccessfulLogin ) )
				{
					// Log to database on successful logon of the user
					//
					doDatabaseBookKeeping( txnSession, loginAttempt, true, strSuccessfulLogonForDB );
				}
				else
				{
					if ( NCashConstant.ADMINISTRATOR_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) || NCashConstant.DEFAULT_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) )
						setAdminPasswordAtFirstLogin( txnSession, loginAttempt.getUserTbl() );
				}

				//if ( !doFirstTimeLoginValidation(hibernateSession, loginAttempt, dtLastSuccessfulLogin) )

				// Check for password of user nearing the expiry time set in the system
				// If the call to api fails then we return from the validation process
				//
				if ( !doPasswordNearingExpiryCheck( txnSession, loginAttempt ) )
				{
					return loginAttempt.isSuccess();
				} //if ( !doPasswordNearingExpiryCheck(hibernateSession, loginAttempt) )
			}
			else
			{
				// Do validations for number of login attempts, exceeding attempt qualifying for
				// user login disabling.
				//
				doFailedLoginValidation( txnSession, loginAttempt, dtLastSuccessfulLogin );
			} //if ( loginAttempt.getUserTbl().getUsrPassword().equals( loginAttempt.getHashedPassword() ) )
		} // end of try block

		catch ( HibernateException e )
		{
			throw new UserPasswordValidationException( "Error occured whilst validating the login attempt for user %1", e, loginAttempt.getUserTbl().getUsrName() );
		} //catch (HibernateException e )

		finally
		{

		}

		return loginAttempt.isSuccess();
	}//public boolean validateLoginAttempt(LoginAttempt loginAttempt, UserTbl userTbl) throws UserPasswordValidationException

	@SuppressWarnings( "unchecked" )
	private boolean checkForMultipleLogon( Session hibernateSession, LoginAttempt loginAttempt )
	{
		try
		{

			if ( isMultiLoginAlow )
			{
				String sqlQueryBuffer = "select count(*) from ConnectedUser cu,  UserLoginInfo uli " + "where cu.uliId = uli.uliId and uli.usrId = :userId";

				List<Long> numLogons = hibernateSession.createQuery( sqlQueryBuffer ).setParameter( "userId", loginAttempt.getUserTbl().getUsrId() ).list();

				if ( numLogons.get( 0 ) == 0 )
					return true;

				return false;
			}
			else
			{
				return false;
			}

		}
		catch ( HibernateException hibernateException )
		{
			hibernateException.getMessage();
		}
		catch ( Exception generalException )
		{
			generalException.getMessage();
		}

		return false;
	}

	private boolean disableUserLogin( LoginAttempt loginAttempt )
	{
		Session hibernateSession = null;

		try
		{
			hibernateSession = HibernateSession.openSession();
			if ( hibernateSession != null )
			{
				hibernateSession.refresh( loginAttempt.getUserTbl() );
				loginAttempt.getUserTbl().setUsrDisabledFl( true );
				hibernateSession.beginTransaction();
				hibernateSession.update( loginAttempt.getUserTbl() );
				hibernateSession.flush();
				hibernateSession.getTransaction().commit();
				hibernateSession.close();

				return true;
			}
		}

		catch ( HibernateException e )
		{
			if ( hibernateSession != null && hibernateSession.getTransaction().isActive() )
			{
				hibernateSession.getTransaction().rollback();
			}

		}
		finally
		{
			if ( hibernateSession != null && hibernateSession.isOpen() )
				hibernateSession.close();
		}

		return false;
	}

	@SuppressWarnings( "unchecked" )
	private boolean doPasswordNearingExpiryCheck( Session hibernateSession, LoginAttempt loginAttempt )
	{
		String sqlQueryBuffer = null;
		DateTime dtLastPasswordActivity = null;
		DateTime dtDateTimeAtTheMoment = null;
		long numDaysSinceLastPasswd = 0;

		try
		{
			sqlQueryBuffer = "select max( upw.upwDttm ) from UserPassword upw where upw.upwEffectedUsrId = :usrId )";

			List<DateTime> resultListDateTime = hibernateSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).list();

			if ( resultListDateTime.isEmpty() == false )
			{
				dtLastPasswordActivity = resultListDateTime.get( 0 );
				dtDateTimeAtTheMoment = new DateTime();

				// Check first that the password is well within the warnable or repairable timeframe
				// so that an alert can be given to the user otherwise we just have to disable the
				// user from logging into the system
				//
				numDaysSinceLastPasswd = Days.daysBetween( dtLastPasswordActivity, dtDateTimeAtTheMoment ).getDays();

				if ( numDaysSinceLastPasswd > Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) )
				{
					// The user has expired his password and unfortunately never logged into the system
					// during the warning period, so disable the user.
					//
					if ( NCashConstant.ADMINISTRATOR_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) || NCashConstant.DEFAULT_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) )
					{
						// Here comes the top-brass comedians just take them to the change password screen
						// and log a message to the database
						//
						loginAttempt.setChangePasswordApplicable( true );
						loginAttempt.setMessage( StringUtil.create( "Your password has expired. You must reset your password before continuing." ) );
						loginAttempt.setMessageId( AuthenticateConfigConstant.PASSWORD_EXPIRED );
						doDatabaseBookKeeping( hibernateSession, loginAttempt, true, StringUtil.create( "User '%1' password expired '%2' days ago, password reset recommended.", loginAttempt.getGivenUserName(), numDaysSinceLastPasswd ) );
					}
					else
					{
						if ( disableUserLogin( loginAttempt ) == true )
						{
							loginAttempt.setPasswordExpired( true );
							loginAttempt.failedLogin( StringUtil.create( strUserPasswordExpired, mapPasswordValidationProperty.get( "PasswordWarningInterval" ) ) );
							loginAttempt.setMessageId( AuthenticateConfigConstant.PASSWORD_WILL_EXPIRE );
							loginAttempt.setMessageParameter( new String[]
							{ mapPasswordValidationProperty.get( "PasswordWarningInterval" ) } );
							doDatabaseBookKeeping( hibernateSession, loginAttempt, false, "User login disable due to password expiry" );

							return false;
						} //if ( disableUserLogin( hibernateSession, loginAttempt ) == true )
					} //if ( loginAttempt.getGivenUserName().equals("Administrator") ||
				}
				else if ( numDaysSinceLastPasswd == Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) )
				{
					loginAttempt.setChangePasswordApplicable( true );
					loginAttempt.setMessage( "Your password has expired. You must reset your password before continuing." );
					loginAttempt.setMessageId( AuthenticateConfigConstant.PASSWORD_EXPIRED );
				}
				else if ( ( numDaysSinceLastPasswd >= ( Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) - Integer.parseInt( mapPasswordValidationProperty.get( "PasswordWarningInterval" ) ) ) ) && ( numDaysSinceLastPasswd < Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) ) )
				{
					loginAttempt.setUserInPasswordWarningInterval( true );
					loginAttempt.setMessage( strPasswordExpiryMessage );
					loginAttempt.setMessageId( AuthenticateConfigConstant.PASSWORD_WILL_EXPIRE );
					String parameter = Long.toString( Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) - numDaysSinceLastPasswd );
					loginAttempt.setMessageParameter( new String[]
					{ parameter } );
				}

				return true;
			} //if ( resultListDateTime.isEmpty() != true )
		}
		catch ( HibernateException e )
		{
			if ( hibernateSession != null && hibernateSession.getTransaction().isActive() )
			{
				hibernateSession.getTransaction().rollback();
			}
		} //catch ( HibernateException e )

		return false;
	}//private void doPasswordNearingExpiryCheck( Session hibernateSession, LoginAttempt loginAttempt, DateTime dtLastLoginTime )

	private void doFailedLoginValidation( Session hibernateSession, LoginAttempt loginAttempt, DateTime dtLastLogonTime )
	{
		String sqlQueryBuffer = null;
		long numLoginAttempts = 0;

		try
		{
			// Make necessary housekeeping on the number of attempts to login by the user
			// if the user has done many attempts to login which crosses the system setting
			// then disable the user from further login and set the message
			//
			if ( loginAttempt.isManualLogin() )
			{
				// We count this attempt of login as well hence the initialization to one
				// there after we just add it in the future code.
				//
				numLoginAttempts = 1;

				// Check for number of consecutive login attempts based on which either we fail
				// the login or disable the user from logging in
				//
				if ( dtLastLogonTime == null )
				{
					sqlQueryBuffer = "select count(*) from UserLoginInfo uli where uli.userTbl.id = " + ":usrId and uli.uliSuccessFl = false ";

					numLoginAttempts += ( Long ) hibernateSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).list().get( 0 );
				}
				else
				{
					sqlQueryBuffer = "select count(*) from UserLoginInfo uli where uli.userTbl.id = " + ":usrId and uli.uliSuccessFl = false and uli.uliDttm > :lastLoginDttm";

					numLoginAttempts += ( Long ) hibernateSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).setParameter( "lastLoginDttm", dtLastLogonTime ).list().get( 0 );
				} //if ( dtLastSuccessfulLogin == null )

				// Check for maximum number of login attempts to be allowed first. If the maximum
				// attempts are crossed already then disable the user if not just fail the login
				//
				if ( numLoginAttempts >= Integer.parseInt( mapPasswordValidationProperty.get( "DisableAfterFailedLoginAttempts" ) ) )
				{
					if ( NCashConstant.ADMINISTRATOR_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) || NCashConstant.DEFAULT_USER.equalsIgnoreCase( loginAttempt.getGivenUserName() ) )
					{
						// Here comes the top-brass comedians just take them to the change password screen
						// and log a message to the database
						//
						loginAttempt.setChangePasswordApplicable( true );
						loginAttempt.failedLogin( StringUtil.create( "You have attempted '%1' times with wrong password.", numLoginAttempts ) );
						loginAttempt.setMessageId( AuthenticateConfigConstant.NO_OF_ATTEMPTS );
						loginAttempt.setMessageParameter( new String[]
						{ Long.toString( numLoginAttempts ) } );

						doDatabaseBookKeeping( hibernateSession, loginAttempt, false, StringUtil.create( "You have attempted '%1' times with wrong password.", numLoginAttempts ) );
					}
					else
					{
						if ( disableUserLogin( loginAttempt ) == true )
						{
							loginAttempt.setDisableUserTbl( true );
							loginAttempt.failedLogin( StringUtil.create( "You have attempted '%1' times with wrong password.", numLoginAttempts ) );
							loginAttempt.setMessageId( AuthenticateConfigConstant.NO_OF_ATTEMPTS );
							loginAttempt.setMessageParameter( new String[]
							{ Long.toString( numLoginAttempts ) } );

							doDatabaseBookKeeping( hibernateSession, loginAttempt, false, StringUtil.create( "You have attempted '%1' times with wrong password.User is disabled.", numLoginAttempts ) );
						} //if ( disableUserLogin( hibernateSession, loginAttempt ) == true )
					} //end of if ( loginAttempt.getGivenUserName().equals("Administrator") || ... )
				}
				else
				{
					loginAttempt.failedLogin( StringUtil.create( "Invalid Username or Password " ) );
					loginAttempt.setMessageId( AuthenticateConfigConstant.INVALID_USERNAME_OR_PASSWORD );
					doDatabaseBookKeeping( hibernateSession, loginAttempt, false, strInvalidPasswordMessageForDB );
				} //end of if ( numLoginAttempts >= Integer.parseInt( mapPasswordValidationProperty.get .... )
			} //if ( loginAttempt.isManualLogin() )
		} //end of try block
		catch ( HibernateException e )
		{
			if ( hibernateSession != null && hibernateSession.getTransaction().isActive() )
			{
				hibernateSession.getTransaction().rollback();
			}
		} //catch ( HibernateException e )
		catch ( Exception generalException )
		{
			log.info( generalException.getStackTrace() );
			log.info( generalException.getMessage() );
		} //catch ( Exception generalException )
	}//private void doFailedLoginValidation( Session hibernateSession, LoginAttempt loginAttempt, DateTime dtLastLogonTime )

	private boolean doFirstTimeLoginValidation( Session hibernateSession, LoginAttempt loginAttempt, DateTime dtLastLogonTime )
	{
		if ( "N".equals( mapPasswordValidationProperty.get( "PasswordFirstLoginChange" ) ) )
		{
			return true;
		}
		try
		{
			String sqlQueryBuffer = "select upw.upwChangingUsrId from UserPassword upw where upw.upwEffectedUsrId = :usrId and upw.upwId =(select max(upw.upwId) from UserPassword upw where upw.upwEffectedUsrId = :usrId)";

			Integer changingUsrId = ( Integer ) hibernateSession.createQuery( sqlQueryBuffer ).setParameter( "usrId", loginAttempt.getUserTbl().getUsrId() ).list().get( 0 );
			if ( changingUsrId == null )
				changingUsrId = loginAttempt.getUserTbl().getUsrId();

			if ( dtLastLogonTime == null || loginAttempt.getUserTbl().getUsrPassword().equals( hashPassword( mapPasswordValidationProperty.get( "PasswordDefault" ) ) ) || ( loginAttempt.getUserTbl().getUsrId() != changingUsrId ) )
			{
				loginAttempt.setChangePasswordApplicable( true );
				loginAttempt.setMessage( "Your password has expired. You must reset your password before continuing." );
				loginAttempt.setMessageId( AuthenticateConfigConstant.PASSWORD_EXPIRED );

				return ( doDatabaseBookKeeping( hibernateSession, loginAttempt, true, strSuccessfulLogonForDB ) );
			}
		}
		catch ( Exception e )
		{
			return false;
		}

		return false;
	}

	private String hashPassword( String unhashedPassword )
	{
		if ( unhashedPassword == null )
		{
			return null;
		}

		// hash the password
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance( "SHA-512" );
			messageDigest.update( unhashedPassword.getBytes( "UTF-16BE" ) );
		}
		catch ( NoSuchAlgorithmException e )
		{
			throw new NCashRuntimeException( e );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new NCashRuntimeException( e );
		}

		return BinaryHelper.byteArrayToHexString( messageDigest.digest() );
	}//private String hashPassword( String unhashedPassword )

	public boolean doDatabaseBookKeeping( Session hibernateSession, LoginAttempt loginAttempt, boolean bSuccessFlag, String strMessage )
	{
		if ( skipServerAudit )
			return true;
		try
		{
			UserLoginInfo userLogin = HibernateSession.createObject( UserLoginInfo.class );

			if ( userLogin != null )
			{
				userLogin.setCreatedDttm( new DateTime() );
				userLogin.setUliDttm( new DateTime() );
				userLogin.setUserTbl( loginAttempt.getUserTbl() );
				userLogin.setUliSuccessFl( bSuccessFlag );
				userLogin.setUliMessage( strMessage );
				userLogin.setUliSourceAddress( loginAttempt.getClientIpAddress() );
				userLogin.setUliSourceHostname( loginAttempt.getClientHostName() );
				userLogin.setPartitionId( loginAttempt.getUserTbl() != null ? loginAttempt.getUserTbl().getPartitionId() : 1 );

				hibernateSession.save( userLogin );
				hibernateSession.flush();
				loginAttempt.setUsrLoginId( userLogin.getId() );

				return true;
			}
		}

		catch ( HibernateException e )
		{
			if ( hibernateSession != null && hibernateSession.getTransaction().isActive() )
			{
				hibernateSession.getTransaction().rollback();
			}
		}

		return false;
	}

	/*public void initialise( PasswordValidationInterface userPasswordValidation ) throws UserPasswordValidationException
	{
		createPropertyInstMap( userPasswordValidation.getPropertyInstGroup() );
	
		if ( userPasswordValidation.getUserStrongPassword() != null )
		{
			isStrongPasswordConfigured = true;
			createStrongPasswordPropertyInstMap( userPasswordValidation.getUserStrongPassword().getPropertyInstGroup() );
		}
	
	}*/

	public String getDefaultPassword()
	{
		return mapPasswordValidationProperty.get( "PasswordDefault" );
	}

	public boolean isLoginAuthenticated( ApplicationTbl appObj )
	{
		return false;
	}

	public boolean isPasswordValid( Session txnSession,String password, List<String> reason, UserTbl userTbl )
	{
		if ( mapPasswordValidationProperty.get( "PasswordMinimumLength" ) != null && password.length() < Integer.parseInt( mapPasswordValidationProperty.get( "PasswordMinimumLength" ) ) )
		{
			reason.add( "password-minchars" );
			reason.add( mapPasswordValidationProperty.get( "PasswordMinimumLength" ) );
			return false;
		}
		if ( mapPasswordValidationProperty.get( "PasswordMaximumLength" ) != null && password.length() > Integer.parseInt( mapPasswordValidationProperty.get( "PasswordMaximumLength" ) ) )
		{
			reason.add( "password-maxchars" );
			reason.add( mapPasswordValidationProperty.get( "PasswordMaximumLength" ) );
			return false;
		}
		if ( !StringHelper.checkWhiteSpace( password ) )
		{
			reason.add( "password-whitesapce" );
			return false;
		}
		if ( "Y".equals( mapPasswordValidationProperty.get( "PasswordForceStrongPassword" ) ) )
		{
			if ( !isStrongPasswordConfigured )
				return defaultStrongPasswordCheck( password, reason, userTbl );
			return strongPasswordCheck( password, reason, userTbl );
		}
		if ( "Y".equals( mapPasswordValidationProperty.get( "PasswordForceAlphaNumeric" ) ) )
		{
			if ( !StringHelper.hasLettersAndDigits( password ) )
			{
				reason.add( "password-atleast" );
				return false;
			}
		}

		return passwordReusabilityValidation( password, reason, userTbl );
	}

	protected boolean passwordReusabilityValidation( String password, List<String> reason, UserTbl userTbl )
	{
		if ( mapPasswordValidationProperty.get( "PasswordMinimumLength" ) != null && Integer.parseInt( mapPasswordValidationProperty.get( "PasswordMinimumLength" ) ) > 0 )
		{
			if ( userTbl != null )
			{
				// get the last time this password was used
				UserPassword upwObj = getLastUserPasswordMatch( SecurityHelper.hashPassword( password ), userTbl );
				if ( upwObj != null )
				{
					// work out how long it has been since this password was last used
					DateTime span = upwObj.getUpwDttm();
					DateTime sysDate = new DateTime( System.currentTimeMillis() );

					// if the unique period has not been elapsed then fail validation
					if ( DateHelper.dayDiff( span, sysDate ) < Integer.parseInt( mapPasswordValidationProperty.get( "PasswordUniquePeriod" ) ) )
					{
						reason.add( "password-used" );
						reason.add( mapPasswordValidationProperty.get( "PasswordUniquePeriod" ) );
						return false;
					}
				}

				List<UserPassword> userPasswords = getPreviousPasswords( userTbl );
				String hashPassword = SecurityHelper.hashPassword( password );
				if ( userPasswords != null )
				{
					int count = 1;
					for ( UserPassword userPassword : userPasswords )
					{
						if ( count > Integer.parseInt( mapPasswordValidationProperty.get( "PasswordPreviousReused" ) ) )
							break;
						if ( userPassword.getUpwPassword().equals( hashPassword ) )
						{
							reason.add( "password-reuse" );
							reason.add( mapPasswordValidationProperty.get( "PasswordPreviousReused" ) );
							return false;
						}
						count++;
					}
				}
			}
		}

		return true;
	}

	private boolean defaultStrongPasswordCheck( String password, List<String> reason, UserTbl userTbl )
	{

		boolean isPasswordStrong = StringHelper.hasLettersAndDigits( password ) && StringHelper.checkUpperAndLower( password ) && StringHelper.checkSpecialCharacters( password, StringHelper.SPECIAL_CHARACTERS );

		if ( !isPasswordStrong )
		{
			reason.add( "strongpassword" );
			return false;
		}

		if ( userTbl == null )
			return true;

		String username = userTbl.getUsrName();
		if ( password.equals( username ) )
		{
			reason.add( "password-same-username" );
			return false;
		}
		if ( password.equals( StringHelper.reverse( username ) ) )
		{
			reason.add( "password-reverse" );
			return false;
		}
		for ( int i = 0; i < username.length() - 2; i++ )
		{
			if ( password.toLowerCase().indexOf( ( ( String ) username.subSequence( i, i + 3 ) ).toLowerCase() ) != -1 )
			{
				reason.add( "password-substring" );
				return false;
			}

		}
		return passwordReusabilityValidation( password, reason, userTbl );
	}

	private boolean strongPasswordCheck( String password, List<String> reason, UserTbl userTbl )
	{

		if ( mapStringPasswordProperty.get( "PasswordMinimumNumberLength" ) != null && !StringHelper.hasNDigits( password, Integer.parseInt( mapStringPasswordProperty.get( "PasswordMinimumNumberLength" ) ) ) )
		{
			reason.add( "password-min-digits" );
			reason.add( mapStringPasswordProperty.get( "PasswordMinimumNumberLength" ) );
			return false;
		}
		if ( mapStringPasswordProperty.get( "PasswordMinimumAlphabetLength" ) != null && !StringHelper.hasNAlphabets( password, Integer.parseInt( mapStringPasswordProperty.get( "PasswordMinimumAlphabetLength" ) ) ) )
		{
			reason.add( "password-min-alphabets" );
			reason.add( mapStringPasswordProperty.get( "PasswordMinimumAlphabetLength" ) );
			return false;
		}
		if ( mapStringPasswordProperty.get( "PasswordMinimumSpecialCharacterLength" ) != null && !StringHelper.hasNSpecialCharacters( password, StringHelper.SPECIAL_CHARACTERS, Integer.parseInt( mapStringPasswordProperty.get( "PasswordMinimumSpecialCharacterLength" ) ) ) )
		{
			reason.add( "password-min-special-character" );
			reason.add( mapStringPasswordProperty.get( "PasswordMinimumSpecialCharacterLength" ) );
			return false;
		}
		if ( mapStringPasswordProperty.get( "PasswordMinimumUpperCaseLength" ) != null && !StringHelper.hasNUpperCase( password, Integer.parseInt( mapStringPasswordProperty.get( "PasswordMinimumUpperCaseLength" ) ) ) )
		{
			reason.add( "password-min-uppercase" );
			reason.add( mapStringPasswordProperty.get( "PasswordMinimumUpperCaseLength" ) );
			return false;
		}
		if ( mapStringPasswordProperty.get( "PasswordMinimumLowerCaseLength" ) != null && !StringHelper.hasNLowerCase( password, Integer.parseInt( mapStringPasswordProperty.get( "PasswordMinimumLowerCaseLength" ) ) ) )
		{
			reason.add( "password-min-lowercase" );
			reason.add( mapStringPasswordProperty.get( "PasswordMinimumLowerCaseLength" ) );
			return false;
		}

		if ( userTbl == null )
			return true;

		String username = userTbl.getUsrName();
		if ( "N".equals( mapStringPasswordProperty.get( "PasswordAllowUsername" ) ) && password.equals( username ) )
		{
			reason.add( "password-same-username" );
			return false;
		}

		if ( "N".equals( mapStringPasswordProperty.get( "PasswordAllowReverseUsername" ) ) && password.equals( StringHelper.reverse( username ) ) )
		{
			reason.add( "password-reverse" );
			return false;
		}

		if ( "N".equals( mapStringPasswordProperty.get( "PasswordAllowSubstringUsername" ) ) )
		{
			for ( int i = 0; i < username.length() - 2; i++ )
			{
				if ( password.toLowerCase().indexOf( ( ( String ) username.subSequence( i, i + 3 ) ).toLowerCase() ) != -1 )
				{
					reason.add( "password-substring" );
					return false;
				}

			}
		}
		return passwordReusabilityValidation( password, reason, userTbl );
	}

	private List<UserPassword> getPreviousPasswords( UserTbl userTable )
	{
		String query = "from UserPassword upw " + " where upw.upwEffectedUsrId = :userId  order by upw.upwDttm desc";
		List<UserPassword> results = HibernateSession.query( query, new String[]
		{ "userId" }, new Object[]
		{ userTable.getUsrId() } );

		return ( results.size() == 0 ) ? null : results;
	}

	private UserPassword getLastUserPasswordMatch( String usrHashedPassword, UserTbl userTable )
	{
		String query = "from UserPassword upw " + " where upw.upwEffectedUsrId = :userId " + " and upw.upwPassword = :upwPassword " + " and upw.upwDttm = ( select max(inr.upwDttm) " + " from UserPassword inr " + " where inr.upwEffectedUsrId = :userId " + " and inr.upwPassword = :upwPassword " + ") ";
		List< ? > results = HibernateSession.query( query, new String[]
		{ "userId", "upwPassword" }, new Object[]
		{ userTable.getUsrId(), usrHashedPassword } );

		// if we get no matches then this password has never been used else return the last time this password was used
		return ( results.size() == 0 ) ? null : ( UserPassword ) results.get( 0 );
	}

	private boolean isPasswordResetIntervalPassed( String usrHashedPassword, UserTbl userTable )
	{
		boolean expired = false;
		UserPassword upwObj = getLastUserPasswordMatch( usrHashedPassword, userTable );
		if ( upwObj != null )
		{
			DateTime span = upwObj.getUpwDttm();
			DateTime sysDate = new DateTime( System.currentTimeMillis() );

			if ( DateHelper.secondDiff( span, sysDate ) > Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) * 24 * 60 * 60 )
				expired = true;
		}
		return expired;
	}

	@SuppressWarnings( "unchecked" )
	public boolean isPasswordExpired( LoginAttempt loginAttempt )
	{
		boolean retVal = true;

		UserPassword userPassword = getLastUserPasswordMatch( loginAttempt.getHashedPassword(), loginAttempt.getUserTbl() );
		if ( userPassword != null )
		{
			String query = "from UserPassword upw where upw.upwEffectedUsrId = :userId ";
			List<UserPassword> results = HibernateSession.query( query, new String[]
			{ "userId" }, new Object[]
			{ loginAttempt.getUserTbl().getUsrId() } );

			retVal = userPassword.getUpwResetFl() || ( results.size() == 1 ); /*|| this.validateLoginAttempt( loginAttempt );*/
			retVal = retVal || isPasswordResetIntervalPassed( loginAttempt.getHashedPassword(), loginAttempt.getUserTbl() );
		}
		return retVal;
	}

	public Integer getWarningResetIntervalDiff( LoginAttempt loginAttempt )
	{
		Integer daysLeft = null;
		UserPassword upwObj = getLastUserPasswordMatch( loginAttempt.getHashedPassword(), loginAttempt.getUserTbl() );
		if ( upwObj != null )
		{
			DateTime span = upwObj.getUpwDttm();
			DateTime sysDate = new DateTime( System.currentTimeMillis() );

			if ( DateHelper.secondDiff( span, sysDate ) > ( Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) - Integer.parseInt( mapPasswordValidationProperty.get( "PasswordWarningInterval" ) ) ) * 24 * 60 * 60 )
			{
				daysLeft = Integer.parseInt( mapPasswordValidationProperty.get( "PasswordResetInterval" ) ) - DateHelper.dayDiff( span, sysDate );
			}
		}
		return daysLeft;
	}

	private void setAdminPasswordAtFirstLogin( Session hibernateSession, UserTbl usrObj )
	{
		String query = "from UserLoginInfo uli where uli.uliSuccessFl= 'Y' and uli.usrId = :UsrId ";

		List<UserLoginInfo> results = HibernateUtil.query(hibernateSession, query, "UsrId", usrObj.getUsrId() );

		if ( results.size() == 1 )
		{
			String updateQuery = "from UserPassword upw where upw.effectedUserTbl.id = :UsrId ";

			UserPassword upwObj = ( UserPassword ) hibernateSession.createQuery( updateQuery ).setParameter( "UsrId", usrObj.getUsrId() ).list().get( 0 );
			try
			{
				upwObj.setUpwDttm( new DateTime() );
				upwObj.setUpwResetFl( true );
				hibernateSession.saveOrUpdate( upwObj );
				hibernateSession.flush();
			}
			catch ( HibernateException e )
			{
				if ( hibernateSession.getTransaction().isActive() )
				{
					hibernateSession.getTransaction().rollback();
				}
			}
		}
	}

	public boolean isSysLoginInformation()
	{
		return sysLoginInformation;
	}

	public void setSysLoginInformation( boolean sysLoginInformation )
	{
		this.sysLoginInformation = sysLoginInformation;
	}
}