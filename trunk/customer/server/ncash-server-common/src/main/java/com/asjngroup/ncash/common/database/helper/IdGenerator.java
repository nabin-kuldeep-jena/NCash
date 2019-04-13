package com.asjngroup.ncash.common.database.helper;

import org.hibernate.HibernateException;

public interface IdGenerator
{
	public void setAllocationSize( String objectKey, long allocSize );

	public Integer generate( String objectKey, int required ) throws HibernateException;

	public Integer generate( String objectKey ) throws HibernateException;

	public Long generateLong( String objectKey, long required ) throws HibernateException;

	public Long generateLong( String objectKey ) throws HibernateException;

	public Long getBlock( String objectKey, long allocSize ) throws HibernateException;
}
