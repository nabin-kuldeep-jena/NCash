package com.asjngroup.ncash.common.database.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.type.YesNoType;

import java.util.HashMap;
import java.util.Map;

import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import com.asjngroup.ncash.common.database.hibernate.util.HibernateInfo;

public class NCashSessionFactoryBean extends LocalSessionFactoryBean
{
	@Override
	protected SessionFactory buildSessionFactory( LocalSessionFactoryBuilder sfb )
	{
		HibernateInfo.setConfiguration( getConfiguration() );
		Map filterParameters = new HashMap();
		filterParameters.put( "partitionIds", YesNoType.INSTANCE );
		FilterDefinition filterDefinition = new FilterDefinition( "partitionFilter", "ptn_id in ( :partitionIds )", filterParameters );

		sfb.addFilterDefinition( filterDefinition );

		filterParameters = new HashMap();
		filterParameters.put( "deleteFl", YesNoType.INSTANCE );
		filterDefinition = new FilterDefinition( "deletedFilter", "delete_fl = :deleteFl", filterParameters );

		sfb.addFilterDefinition( filterDefinition );
		return super.buildSessionFactory( sfb );
	}
}
