package com.asjngroup.ncash.common.models;

import java.io.Serializable;
import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.references.ApplicationTbl;
import com.asjngroup.ncash.common.database.hibernate.references.ConnectedUser;
import com.asjngroup.ncash.common.database.hibernate.references.UserLoginInfo;
import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;

public class UserSession implements Serializable
{
	private static final long serialVersionUID = -8693539513620777310L;

	private UserTbl userTbl;
	
	private UserLoginInfo userLoginInfo;
	
	private List<Integer> userPartitionIDs;

	//private Collection<UserRolePartition> userRolePartitions;

	private ApplicationTbl applicationTbl;

	private Boolean isRTL = false;

	private Boolean isIslamicCalendar = false;

	private String locale = "en";

	private String remoteAddr = "localhost";

	private String remoteHost = "127.0.0.1";

	private String messageText = null;

	private String lastLogonText = null;

	private boolean changePasswordApplicable = false;

	private boolean scObserverFl = true;

	private String[] messageParameter = null;

	private String decimalSeperator = ".";

	private String groupSeperator = ",";

	private boolean loginhappened = false;

	private boolean isSSO;

	private String logoutUrl;

	private String sessionId;

	public UserSession()
	{

	}

	public UserTbl getUserTbl()
	{
		return userTbl;
	}

	public void setUserTbl( UserTbl userTbl )
	{
		this.userTbl = userTbl;
	}

	public String getLocale()
	{
		return locale;
	}

	public void setLocale( String locale )
	{
		this.locale = locale;
	}

	public ApplicationTbl getApplicationTbl()
	{
		return applicationTbl;
	}

	public void setApplicationTbl( ApplicationTbl applicationTbl )
	{
		this.applicationTbl = applicationTbl;
	}

	public Boolean isRTL()
	{
		return this.isRTL;
	}

	public void setRTL( Boolean isRTL )
	{
		this.isRTL = isRTL;
	}

	public Boolean isIslamicCalendar()
	{
		return this.isIslamicCalendar;
	}

	public void setisIslamicCalendar( Boolean isIslamicCalendar )
	{
		this.isIslamicCalendar = isIslamicCalendar;
	}

	public String[] getMessageParameter()
	{
		return messageParameter;
	}

	public void setMessageParameter( String[] messageParameter )
	{
		this.messageParameter = messageParameter;
	}

	/*@SuppressWarnings( "unchecked" )
	public List<Integer> getTeamIDs()
	{
		Session session = HibernateSession.getSession();
		List<Integer> teamIDs;
		try
		{
			teamIDs = session.createQuery( "select tu.teaId from TeamUser tu where tu.usrId=:id " ).setParameter( "id", getUserTbl().getId() ).list();
		}
		finally
		{
			session.close();
		}
		return teamIDs;
	}
	
	public List<Integer> getUserPartitionIDs()
	{
		userPartitionIDs = new ArrayList<Integer>();
		Session session = HibernateSession.getSession();
		try
		{
			for ( UserRolePartition urp : getUserRolePartitions( session ) )
			{
				userPartitionIDs.add( urp.getUrpPtnId() );
			}
		}
		finally
		{
			session.close();
		}
		return userPartitionIDs;
	}
	
	public List<Integer> getUserPartitionIDs( Session session )
	{
		userPartitionIDs = new ArrayList<Integer>();
		for ( UserRolePartition urp : getUserRolePartitions( session ) )
		{
			userPartitionIDs.add( urp.getUrpPtnId() );
		}
		return userPartitionIDs;
	}
	
	@SuppressWarnings( "unchecked" )
	public Collection<UserRolePartition> getUserRolePartitions( Session session )
	{
		UserTbl currentUserTbl = null;
		List<UserTbl> userTable;
		userTable = ( session.createQuery( "from UserTbl where id=:id" ).setParameter( "id", getUserTbl().getId() ).list() );
		if ( userTable.size() != 0 )
		{
			currentUserTbl = userTable.get( 0 );
	
		}
		if ( currentUserTbl != null )
		{
			this.userRolePartitions = currentUserTbl.getUserRolePartitions();
		}
		return this.userRolePartitions;
	}*/

	public void setChangePasswordApplicable( boolean changePasswordApplicable )
	{
		this.changePasswordApplicable = changePasswordApplicable;
	}

	public boolean isChangePasswordApplicable()
	{
		return changePasswordApplicable;
	}

	public String getRemoteAddr()
	{
		return remoteAddr;
	}

	public void setRemoteAddr( String remoteAddr )
	{
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteHost()
	{
		return remoteHost;
	}

	public void setRemoteHost( String remoteHost )
	{
		this.remoteHost = remoteHost;
	}

	public void setMessageText( String messageText )
	{
		this.messageText = messageText;
	}

	public String getMessageText()
	{
		return messageText;
	}

	public void setLastLogonText( String lastLogonText )
	{
		this.lastLogonText = lastLogonText;
	}

	public String getLastLogonText()
	{
		return lastLogonText;
	}

	public void setScObserverFl( boolean scObserverFl )
	{
		this.scObserverFl = scObserverFl;
	}

	public boolean getScObserverFl()
	{
		return this.scObserverFl;
	}

	public void setDecimalSeperator( String decimalSeperator )
	{
		this.decimalSeperator = decimalSeperator;
	}

	public String getDecimalSeperator()
	{
		return decimalSeperator;
	}

	public void setGroupSeperator( String groupSeperator )
	{
		this.groupSeperator = groupSeperator;
	}

	public String getGroupSeperator()
	{
		return groupSeperator;
	}

	public void setLoginhappened( boolean loginhappened )
	{
		this.loginhappened = loginhappened;
	}

	public boolean isLoginhappened()
	{
		return loginhappened;
	}

	public boolean isSSO()
	{
		return isSSO;
	}

	public void setSSO( boolean isSSO )
	{
		this.isSSO = isSSO;
	}

	public String getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl( String logoutUrl )
	{
		this.logoutUrl = logoutUrl;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId( String sessionId )
	{
		this.sessionId = sessionId;
	}

	public List<Integer> getUserPartitionIDs()
	{
		return userPartitionIDs;
	}

	public void setUserPartitionIDs( List<Integer> userPartitionIDs )
	{
		this.userPartitionIDs = userPartitionIDs;
	}

	public UserLoginInfo getUserLoginInfo()
	{
		return userLoginInfo;
	}

	public void setUserLoginInfo( UserLoginInfo userLoginInfo )
	{
		this.userLoginInfo = userLoginInfo;
	}

}