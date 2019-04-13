package com.asjngroup.ncash.product.controller;

import java.util.List;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.asjngroup.ncash.common.database.hibernate.references.ProductTbl;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateUtil;

public class ProductServiceImpl implements ProductService
{

	public ProductTbl getProduct( String storeId, String itemId, MessageContext context )
	{
		Session session = null;
		ProductTbl product = null;

		try
		{
			session = HibernateSession.openSession();
			List<ProductTbl> products = HibernateUtil.query( session, "from ProductTbl prd where prd.strId=:storeId and prd.prdId=:itemId", new String[]
			{ "storeId", "itemId" }, new Object[]
			{ storeId, itemId } );

			if ( !products.isEmpty() && products.size() == 1 )
				product = products.get( 0 );
		}
		catch ( HibernateException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( session != null && session.isOpen() )
				session.close();
		}

		return product;
	}

}
