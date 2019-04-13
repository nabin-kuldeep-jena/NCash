package com.asjngroup.ncash.store.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.asjngroup.ncash.common.database.hibernate.references.StoreBranch;
import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;
import com.asjngroup.ncash.store.service.models.StoreTblModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Api( value = "/store", consumes = "application/json", tags =
{ "User Management" }, produces = "application/json", description = "This api provided user related services like register user,enable user,forgot password" )
public class StoreManagementController
{
	@Autowired( required = true )
	StoreManagementService storeManagementService;

	@GET
	@Path( NCashServicesConstant.STORE_ALL_PREFERED_URL_RESOURCE )
	@Produces( MediaType.APPLICATION_JSON )
	@ApiOperation( value = "returns list of stores", response = ResponseMessageModel.class )
	public List<StoreTblModel> getAllPreferedStore( @QueryParam( "city" ) String city, @Context MessageContext context )
	{
		List<StoreBranch> stores = storeManagementService.getAllPreferedStore( city, context );
		if ( !stores.isEmpty() )
			return getStoreModels( stores );

		return null;
	}

	private List<StoreTblModel> getStoreModels( List<StoreBranch> stores )
	{
		List<StoreTblModel> storeModels = new ArrayList<StoreTblModel>();
		for ( StoreBranch store : stores )
		{
			StoreTblModel model = new StoreTblModel();
			model.setStrName( store.getStoreTbl().getStrName() );
			model.setStrAddress( store.getAddress().getAdrStreet1() );
			model.setImgPath( store.getStoreTbl().getImageTbl().getImgPath() );

			storeModels.add( model );
		}

		return storeModels;
	}

	public StoreManagementService getStoreManagementService()
	{
		return storeManagementService;
	}

	public void setStoreManagementService( StoreManagementService storeManagementService )
	{
		this.storeManagementService = storeManagementService;
	}

}
