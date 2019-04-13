package com.asjngroup.ncash.security.dao;

import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.references.EmailValidationToken;

public class EmailValidationTokenDAO
{
	public static List<EmailValidationToken> getEmailValidationTokenByEmailId( String emailId, String evtToken, Session session )
	{
		Query query = session.createQuery( "from EmailValidationToken evt where evt.userTbl.usrEmailAddress=:usrEmailAddress and evt.evtToken=:evtToken order by evt.evtExpireDttm " );
		query.setString( "usrEmailAddress", emailId );
		query.setString( "evtToken", evtToken );
		return query.list();
	}
}
