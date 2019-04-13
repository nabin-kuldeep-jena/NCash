package com.asjngroup.ncash.common.util;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


public class ObjectHelper
{
    public static Object[] getObjectsOfClass( Object[] hayStack, Class clazz )
    {
        List list = new ArrayList();

        for ( int i = 0; i < hayStack.length; i++ )
        {
            if ( clazz.isInstance( hayStack[ i ] ) )
            {
                list.add( hayStack[ i ] );
            }
        }

        return list.toArray();
    }

    public static Map< Class, List< Object > > buildClassObjectMap( List< Object > objs )
    {
        return buildClassObjectMap( objs.toArray() );
    }

    public static Map< Class, List< Object > > buildClassObjectMap( Object[] objs )
    {
        Map< Class, List< Object > > classObjectMap = new HashMap< Class, List< Object > >();

        for ( Object obj : objs )
        {
            Class clazz = obj.getClass();
            if ( classObjectMap.containsKey( clazz ) )
            {
                classObjectMap.get( clazz ).add( obj );
            }
            else
            {
                List< Object > objList = new ArrayList< Object >();
                objList.add( obj );
                classObjectMap.put( clazz, objList );
            }
        }

        return classObjectMap;
    }

    public static Object[] getObjectsNotOfClass( Object[] hayStack, Class clazz )
    {
        List list = new ArrayList();

        for ( int i = 0; i < hayStack.length; i++ )
        {
            if ( !clazz.isInstance( hayStack[ i ] ) )
            {
                list.add( hayStack[ i ] );
            }
        }

        return list.toArray();
    }

    public static int[] getIndicesOfClass( Object[] hayStack, Class clazz )
    {
        List list = new ArrayList();

        for ( int i = 0; i < hayStack.length; i++ )
        {
            if ( clazz.isInstance( hayStack[ i ] ) )
            {
                list.add( new Integer( i ) );
            }
        }

        int[] result = new int[list.size()];

        for ( int i = 0; i < list.size(); i++ )
        {
            result[ i ] = ( (Integer)list.get( i ) ).intValue();
        }

        return result;
    }

    public static List filterObjectCollectionByClass( Collection hayStack, Class clazz )
    {
        List al = new ArrayList();

        Iterator it = hayStack.iterator();

        while ( it.hasNext() )
        {
            Object obj = it.next();

            if ( obj.getClass().equals( clazz ) )
            {
                al.add( obj );
            }
        }

        hayStack.removeAll( al );

        return al;
    }

    public static String getClassOnlyName( Object object )
    {
        return getClassOnlyName( object.getClass() );
    }

    public static String getClassOnlyName( Class clazz )
    {
        String classFullName = clazz.getName();

        return classFullName.substring( classFullName.lastIndexOf( "." ) + 1 );
    }

    public static Class primitiveClassToWrapperClass( Class clazz )
    {
        if ( !clazz.isPrimitive() )
            return clazz;

        if ( clazz.equals( int.class ) )
        {
            return Integer.class;
        }
        else if ( clazz.equals( boolean.class ) )
        {
            return Boolean.class;
        }
        else if ( clazz.equals( long.class ) )
        {
            return Long.class;
        }
        else if ( clazz.equals( short.class ) )
        {
            return Short.class;
        }
        else if ( clazz.equals( byte.class ) )
        {
            return Byte.class;
        }
        else if ( clazz.equals( char.class ) )
        {
            return Character.class;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown primitive class: " + clazz.getName() );
        }
    }

    public static <T> boolean cloneGettersAndSetters( T oldObject, T newObject )
    {
        Class oldClazz = oldObject.getClass();
        Class newClazz = newObject.getClass();

        Method[] methods = oldClazz.getMethods();

        for ( Method method : methods )
        {
            if ( method.getName().startsWith( "get" ) )
            {
                String methodName = method.getName().substring( 3 );

                try
                {
                    Method toMethod = newClazz.getMethod( "set" + methodName, new Class[] { method.getReturnType() } );
                    toMethod.invoke( newObject, method.invoke( oldObject ) );
                }
                catch ( NoSuchMethodException e )
                {
                    continue;
                }
                catch ( IllegalAccessException e )
                {
                    return false;
                }
                catch ( InvocationTargetException e )
                {
                    continue;
                }
            }
        }

        return true;
    }

    public static byte[] intToByteArray( long l )
    {
        byte[] arr = new byte[4];

        arr[ 0 ] = (byte)( l >>> 24 );
        arr[ 1 ] = (byte)( l >>> 16 );
        arr[ 2 ] = (byte)( l >>> 8 );
        arr[ 3 ] = (byte)( l >>> 0 );

        return arr;
    }

    public static byte[] longToByteArray( long l )
    {
        byte[] arr = new byte[8];

        arr[ 0 ] = (byte)( l >>> 56 );
        arr[ 1 ] = (byte)( l >>> 48 );
        arr[ 2 ] = (byte)( l >>> 40 );
        arr[ 3 ] = (byte)( l >>> 32 );
        arr[ 4 ] = (byte)( l >>> 24 );
        arr[ 5 ] = (byte)( l >>> 16 );
        arr[ 6 ] = (byte)( l >>> 8 );
        arr[ 7 ] = (byte)( l >>> 0 );

        return arr;
    }

    public static boolean equals( Object obj1, Object obj2 )
    {
        if ( obj1 == null )
            return ( obj2 == null );
        else
            return obj1.equals( obj2 );
    }
    
    public static boolean equalsToString(Object obj1, Object obj2)
    {
        if (obj1 == null)
            return (obj2 == null);
        else if (obj2 == null)
        	return false;
        else
            return obj1.toString().equals(obj2.toString());
    }

    public static int compare( Object obj1, Object obj2 )
    {
        if ( obj1 == null && obj2 == null )
            return 0;

        if ( obj1 == null )
            return -1;
        if ( obj2 == null )
            return 1;

        return ( (Comparable)obj1 ).compareTo( obj2 );
    }
}
