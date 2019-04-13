package com.asjngroup.ncash.product.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.database.hibernate.references.ProductTbl;

public interface ProductService
{
	public ProductTbl getProduct( String storeId, String itemId, MessageContext context );
}
