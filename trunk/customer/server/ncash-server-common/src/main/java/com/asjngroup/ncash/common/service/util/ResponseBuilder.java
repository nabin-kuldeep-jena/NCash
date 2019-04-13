package com.asjngroup.ncash.common.service.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.asjngroup.ncash.common.service.models.ResponseMessageModel;

public class ResponseBuilder
{
	public static Response sendErrorResponse( String responseMessage )
	{
		return sendErrorResponse( responseMessage, Status.NOT_ACCEPTABLE, null );

	}

	public static Response sendResponse( ResponseMessageModel responseMessage )
	{
		return Response.status( responseMessage.getStatusCode() ).entity( responseMessage ).build();

	}

	public static Response sendErrorResponse( String responseMessage, Status status, String document )
	{
		ResponseMessageModel errorMessage = new ResponseMessageModel( responseMessage, status.getStatusCode(), document );
		return Response.status( status ).entity( errorMessage ).build();

	}

	public static Response sendAcceptResponse( Object entity )
	{
		return sendAcceptResponse( entity, Status.ACCEPTED );
	}

	public static Response sendAcceptResponse( Object entity, Status accepted )
	{
		ResponseMessageModel responseMessage = new ResponseMessageModel( entity.toString(), accepted.getStatusCode(), "" );
		return Response.status( Status.ACCEPTED ).entity( responseMessage ).build();
	}

	public static Response sendAcceptResponseForEntity( Object entity )
	{
		return sendAcceptResponseForEntity( entity, Status.ACCEPTED );
	}

	public static Response sendAcceptResponseForEntity( Object entity, Status accepted )
	{
		return Response.status( Status.ACCEPTED ).entity( entity ).build();
	}
}
