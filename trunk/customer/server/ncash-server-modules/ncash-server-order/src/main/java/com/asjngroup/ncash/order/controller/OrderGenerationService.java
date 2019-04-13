package com.asjngroup.ncash.order.controller;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;
import com.asjngroup.ncash.order.service.models.OrderCreationResponseModel;
import com.asjngroup.ncash.order.service.models.OrderTblModel;

public interface OrderGenerationService
{

	OrderCreationResponseModel createNewOrder( OrderTblModel orderTblModel, MessageContext context  , ResponseMessageModel errorResponse );

}
