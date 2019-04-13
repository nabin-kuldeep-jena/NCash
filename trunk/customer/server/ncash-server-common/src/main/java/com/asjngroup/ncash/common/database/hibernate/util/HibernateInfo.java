package com.asjngroup.ncash.common.database.hibernate.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;

import com.asjngroup.ncash.common.database.helper.IdGenerator;

public class HibernateInfo
{

	private final SessionFactory sessionFactory;
	private final DataSource dataSource;
	private final IdGenerator idGenerator;
	private static Configuration configuration;

	public HibernateInfo(SessionFactory sessionFactory, DataSource dataSource, IdGenerator idGenerator )
	{
		this.sessionFactory = sessionFactory;
		this.dataSource = dataSource;
		this.idGenerator = idGenerator;
	}

	public SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public IdGenerator getIdGenerator()
	{
		return idGenerator;
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}
	
	
	//need to removed once we get better solution 
	public static void setConfiguration(Configuration conf)
	{
		configuration=conf;
	}
}
