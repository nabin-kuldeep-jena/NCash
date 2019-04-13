package com.asjngroup.ncash.framework.referencedata.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.common.service.util.ResponseBuilder;
import com.asjngroup.ncash.framework.service.util.NCashServicesConstant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Consumes(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Produces(
{ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Path( "/" )
@Api( value = "/country", consumes = "application/json", tags =
{ "User Management" }, produces = "application/json", description = "This api provided user to access all the country details" )
public class CountryController
{
	@Autowired( required = true )
	CountryService countryService;

	@GET
	@Path( NCashServicesConstant.COUNTRY_URL_RESOURCE )
	@ApiOperation( value = "get operation to list down all the countries available in the system .", response = ResponseMessageModel.class )
	public Response fetchAllCountry( @Context MessageContext context )
	{
		ResponseMessageModel responseMessageModel = countryService.getAllCountry( context );
		return ResponseBuilder.sendResponse( responseMessageModel );
	}

	public CountryService getCountryService()
	{
		return countryService;
	}

	public void setCountryService( CountryService countryService )
	{
		this.countryService = countryService;
	}

}
