package com.asjngroup.ncash.security.dao;

import org.hibernate.Session;

import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.references.UserTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;

public class UserDAO
{
	public static List<UserTbl> getUserByName( Session session, String userName )
	{
		return HibernateUtil.query( session, "from UserTbl usr where usr.usrName =:usrName", "usrName", userName );
	}

	public static List<UserTbl> getUniqueUser( Session session, String userName, String mobNo, String emailId )
	{
		return HibernateUtil.query( session, "from UserTbl usr where usr.usrName =:usrName or usr.usrMobNo =:usrMobNo or usr.usrEmailAddress =:usrEmailAddress", new String[]
		{ "usrName", "usrMobNo", "usrEmailAddress" }, new Object[]
		{ userName, mobNo, emailId } );
	}

	public static List<UserTbl> getUserByMobileNo( Session session, String mobNo )
	{
		return HibernateUtil.query( session, "from UserTbl usr where usr.usrMobNo =:mobNo", "mobNo", mobNo );
	}

	public static List<UserTbl> getUserByEmailId( Session session, String emailid )
	{
		return HibernateUtil.query( session, "from UserTbl usr where usr.usrEmailAddress =:emailid", "emailid", emailid );
	}

}
