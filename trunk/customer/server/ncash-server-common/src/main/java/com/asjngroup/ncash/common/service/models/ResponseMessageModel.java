package com.asjngroup.ncash.common.service.models;

import java.util.HashMap;

public class ResponseMessageModel
{
	private String message;
	private HashMap<String, String> errorKey = new HashMap<String, String>();
	private HashMap<String, String> responseData = new HashMap<String, String>();
	private Integer statusCode;
	private String documentation;

	public ResponseMessageModel( int statusCode )
	{
		this.statusCode = statusCode;
	}

	public ResponseMessageModel( String message, Integer statusCode, String documentation )
	{
		this.message = message;
		this.statusCode = statusCode;
		this.documentation = documentation;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public HashMap<String, String> getErrorKey()
	{
		return errorKey;
	}

	public void addError( String errorKey, String message )
	{
		this.errorKey.put( errorKey, message );
	}

	public void addSucessRecord( String sucessKey, String sucessValue )
	{
		this.responseData.put( sucessKey, sucessValue );
	}

	public HashMap<String, String> getResponseData() 
	{
		return responseData;
	}

	public Integer getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode( Integer statusCode )
	{
		this.statusCode = statusCode;
	}

	public void setMessage( Integer statusCode, String message )
	{
		this.statusCode = statusCode;
		this.message = message;
	}

	public String getDocumentation()
	{
		return documentation;
	}

	public void setDocumentation( String documentation )
	{
		this.documentation = documentation;
	}
}
