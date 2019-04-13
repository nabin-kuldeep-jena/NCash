package com.asjngroup.ncash.framework.referencedata.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;

public interface CountryService
{

	ResponseMessageModel getAllCountry( MessageContext context );

}
