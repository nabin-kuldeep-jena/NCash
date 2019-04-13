package com.asjngroup.ncash.framework.security.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NCashCommonFilter implements Filter
{

	private void addCorsHeader( HttpServletResponse response, HttpServletRequest request, boolean isMobileApp, boolean isCors )
	{
		String domain = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + request.getContextPath();
		//TODO: externalize the Allow-Origin
		response.addHeader( "Access-Control-Allow-Origin", request.getHeader( "origin" ) );
		response.addHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD" );
		response.addHeader( "Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept" );
		response.addHeader( "Access-Control-Max-Age", "1728000" );
		response.addHeader( "Access-Control-Allow-Credentials", "true" );
		if ( isMobileApp )
			response.setContentType( "application/json" );
	}

	public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain ) throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = ( HttpServletRequest ) request;
		HttpServletResponse httpServletResponse = ( HttpServletResponse ) response;
		String urlPath = httpServletRequest.getRequestURI();
		boolean isMobileApp = urlPath.contains( "services" );
		addCorsHeader( httpServletResponse, httpServletRequest, isMobileApp, true );
		filterChain.doFilter( httpServletRequest, response );
	}

	public void destroy()
	{
		//  do nothing

	}

	public void init( FilterConfig filterConfig ) throws ServletException
	{
		// TODO Auto-generated method stub
		
	}

}
