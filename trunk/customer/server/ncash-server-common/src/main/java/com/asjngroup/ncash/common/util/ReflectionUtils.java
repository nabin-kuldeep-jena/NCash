package com.asjngroup.ncash.common.util;

import java.lang.reflect.Method;

public final class ReflectionUtils
{

	private ReflectionUtils()
	{
		throw new AssertionError();
	}

	public static Method getMethod( Object obj, String methodName )
	{
		return getMethod( obj.getClass(), methodName );
	}

	public static Method getMethod( Class< ? > clazz, String methodName )
	{
		Method result = findMethod( clazz, methodName );
		return result;
	}

	private static Method findMethod( Class< ? > clazz, String methodName )
	{
		Method[] methods = clazz.getMethods();
		Method result = null;
		for ( int i = 0; i < methods.length; i++ )
		{
			Method method = methods[i];
			if ( method.getName().equals( methodName ) )
			{
				result = method;
			}
		}
		return result;
	}

	public static Object invoke( Method method, Object obj, Object[] args ) throws Exception
	{
		Object result = null;
		try
		{
			result = method.invoke( obj, args );
		}
		catch ( Exception e )
		{
			throw e;
		}
		return result;
	}
}