package com.asjngroup.ncash.product.controller;

import java.math.BigDecimal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.asjngroup.ncash.common.database.hibernate.references.ProductTbl;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.product.service.models.ProductTblModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Api( value = "/", consumes = "application/json", tags =
{ "User Management" }, produces = "application/json", description = "This api returns a product provided its code" )
public class ProductController
{
	@Autowired( required = true )
	ProductService productService;

	public ProductService getProductService()
	{
		return productService;
	}

	public void setProductService( ProductService productService )
	{
		this.productService = productService;
	}

	@GET
	@Path( "/{storeId}/{itemId}" )
	@Produces( MediaType.APPLICATION_JSON )
	@ApiOperation( value = "", response = ResponseMessageModel.class )
	public ProductTblModel getItem( @PathParam( "storeId" ) String storeId, @PathParam( "itemId" ) String itemId, @Context MessageContext context )
	{
		ProductTbl product = productService.getProduct( storeId, itemId, context );
		return getProductModel( product );
	}

	public ProductTblModel getProductModel( ProductTbl productInst )
	{
		ProductTblModel product = new ProductTblModel();
		product.setPrdName( productInst.getPrdName() );
		product.setPrdCode( productInst.getPrdCode() );
		product.setPrdPrice( ( ( BigDecimal ) productInst.getPrdPrice() ).doubleValue() );
		product.setPrdDesc( productInst.getPrdDesc() );
		product.setPrdImgId( productInst.getPrdImgId() );
		product.setPrdExpiryDate( productInst.getPrdExpiryDate() );
		product.setStrId( productInst.getStrId() );

		return product;
	}

}
