package com.asjngroup.ncash.store.controller;

import java.util.List;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.database.hibernate.references.StoreBranch;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;

public class StoreManagementServiceImpl implements StoreManagementService
{

	public List<StoreBranch> getAllPreferedStore( String city, MessageContext context )
	{
		List<StoreBranch> stores = HibernateSession.query( "from StoreBranch stb where stb.address.cityTbl.ctyName = :ctyName", new String[]
		{ "ctyName" }, new Object[]
		{ city.trim().toLowerCase() } );

		return stores;
	}

}
