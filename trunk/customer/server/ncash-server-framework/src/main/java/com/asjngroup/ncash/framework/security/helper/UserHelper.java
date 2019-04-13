package com.asjngroup.ncash.framework.security.helper;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.models.UserSession;
import com.asjngroup.ncash.common.util.NCashConstant;

public class UserHelper
{

	public static UserTbl getCurrentUser( MessageContext context )
	{
		return getCurrentUser( context.getHttpServletRequest() );
	}

	public static UserTbl getCurrentUser( HttpServletRequest servletRequest )
	{
		return getCurrentUserSession( servletRequest.getSession() ).getUserTbl();
	}

	public static UserSession getCurrentUserSession( MessageContext context )
	{
		return getCurrentUserSession( context.getHttpServletRequest() );
	}

	public static UserSession getCurrentUserSession( HttpServletRequest httpServletRequest )
	{
		return getCurrentUserSession( httpServletRequest.getSession() );
	}

	public static UserSession getCurrentUserSession( HttpSession httpSession )
	{
		UserSession userSession = ( UserSession ) httpSession.getAttribute( NCashConstant.USER_SESSION );
		return userSession;
	}

	public static List<Integer> getPartitionIdsForCurrentUser( MessageContext context )
	{
		/*UserSession userSession = getCurrentUserSession( context );
		return userSession.getUserPartitionIDs();*/

		List<Integer> partitionIds = new ArrayList<>();
		partitionIds.add( 1 );
		return partitionIds;
	}

}
