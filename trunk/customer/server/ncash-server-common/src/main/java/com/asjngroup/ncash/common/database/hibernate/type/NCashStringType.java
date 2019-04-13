package com.asjngroup.ncash.common.database.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.NullableType;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

public class NCashStringType implements UserType
{
	private static final int[] TYPES =
	{ 12 };

	public static final NCashStringType STRING_TYPE = new NCashStringType();

	public int[] sqlTypes()
	{
		return TYPES;
	}

	public Class<String> returnedClass()
	{
		return String.class;
	}

	public boolean equals( Object x, Object y ) throws HibernateException
	{
		if ( ( x == null ) && ( y == null ) )
			return true;

		if ( x == null )
			return false;

		if ( ( ( x == "" ) && ( y == null ) ) || ( ( y == "" ) && ( x == null ) ) )
			return true;

		return x.equals( y );
	}

	public int hashCode( Object x ) throws HibernateException
	{
		if ( x == null )
		{
			return 0;
		}
		return x.hashCode();
	}

	public Object deepCopy( Object value ) throws HibernateException
	{
		if ( value == null )
			return null;

		return value;
	}

	public boolean isMutable()
	{
		return false;
	}

	public Serializable disassemble( Object value ) throws HibernateException
	{
		return ( ( String ) value );
	}

	public Object assemble( Serializable cached, Object owner ) throws HibernateException
	{
		return cached;
	}

	public Object replace( Object original, Object target, Object owner ) throws HibernateException
	{
		return original;
	}

	public Object nullSafeGet( ResultSet rs, String[] names, SessionImplementor session, Object owner ) throws HibernateException, SQLException
	{

		String str = rs.getString( names[0] );

		if ( str == null )
			return "";

		return str;
	}

	public void nullSafeSet( PreparedStatement st, Object value, int index, SessionImplementor session ) throws HibernateException, SQLException
	{
		String saveValue = null;
		String val;
		if ( ( value != null ) && ( ( val = value.toString() ) != null ) && ( val.length() != 0 ) )
		{
			saveValue = val;
		}

		StringType.INSTANCE.nullSafeSet( st, saveValue, index, session );
	}

}
