package com.asjngroup.ncash.common.util.collection;

import java.util.*;


public class CollectionHelper
{
    public static final Integer BATCH_SIZE = 200 ;
    
    public static <E> LockList< E > lockList( List< E > c )
    {
        return new BaseLockList< E >( c );
    }

    public static <K, V> LockMap< K, V > lockMap( Map< K, V > c )
    {
        return new BaseLockMap< K, V >( c );
    }

    public static boolean hasNull( Collection c )
    {
        for ( Object obj : c )
        {
            if ( obj == null )
                return true;
        }

        return false;
    }

    public static boolean isOrderedSubset( List smallerList, List biggerList )
    {
        Iterator smallIt = smallerList.iterator();
        Iterator bigIt = biggerList.iterator();

        // while both iterators have something left
        while ( smallIt.hasNext() && bigIt.hasNext() )
        {
            // get the objs
            Object nextSmall = smallIt.next();
            Object nextBig = bigIt.next();

            // while the objs are not equal
            while ( !nextSmall.equals( nextBig ) )
            {
                // if there are no more items in the big list when we
                // have a item in the small list that doesn't match
                if ( !bigIt.hasNext() )
                    return false;

                // get the next big object
                nextBig = bigIt.next();
            }
        }
        if(!smallIt.hasNext() && !bigIt.hasNext())
        	return true;

        // only return true if the small list was exhausted
        return bigIt.hasNext();
    }

    public static int hashCode( Object... objs )
    {
        return Arrays.deepHashCode( objs );
    }

    public static <T> List< T > filterList( List< ? > inputList, Class< T > clazz )
    {
        List< T > list = new ArrayList< T >();

        for ( Object obj : inputList )
        {
            if ( clazz.isInstance( obj ) )
            {
                T castedObj = clazz.cast( obj );
                list.add( castedObj );
            }
        }

        return list;
    }

    public static boolean collectionsEqual( Collection< ? > collection1, Collection< ? > collection2 )
    {
        if ( collection1.size() != collection2.size() )
            return false;

        Collection< ? > copyColl1 = new ArrayList< Object >( collection1 );
        Collection< ? > copyColl2 = new ArrayList< Object >( collection2 );

        // remove each element from the second collection that's in the first collection
        for ( Object obj : copyColl1 )
        {
            if ( !copyColl2.remove( obj ) )
                return false;
        }

        // not sure if this is necessary but does no harm
        if ( copyColl2.size() > 0 )
            return false;

        return true;
    }

    public static Map buildMap( Object[] keys, Object[] values )
    {
        Map map = new HashMap();

        for ( int i = 0; i < keys.length; i++ )
        {
            map.put( keys[ i ], values[ i ] );
        }

        return map;
    }

    public static <T> List< T > unionList( List< ? extends T > list1, List< ? extends T > list2 )
    {
        List< T > result = new ArrayList< T >( list1 );

        for ( T obj : list2 )
        {
            if ( !result.contains( obj ) )
            {
                result.add( obj );
            }
        }

        return result;
    }

    public static <T> List< T > distinctList( List< ? extends T > list )
    {
        List< T > result = new ArrayList< T >();

        for ( T obj : list )
        {
            if ( !result.contains( obj ) )
            {
                result.add( obj );
            }
        }

        return result;
    }

    public static <K, V> Map< K, V > reverseMap( Map< V, K > map )
    {
        Map< K, V > reverseMap = new HashMap< K, V >();

        for ( Map.Entry< V, K > entry : map.entrySet() )
        {
            reverseMap.put( entry.getValue(), entry.getKey() );
        }

        return reverseMap;
    }

    public static String toString( Collection< ? extends Object > c )
    {
    	if(c.isEmpty())
    		return "";
    	
        StringBuilder sb = new StringBuilder();
        for ( Object o : c )
        {
            sb.append( o.toString() );
            sb.append( ", " );
        }

        sb.setLength( sb.length() - 2 );

        return sb.toString();
    }
    
    public static List< List< Integer> > getInClauseList( List<Integer> list , int bactchSize)
    {
	if(bactchSize >= 1000)
	    bactchSize = 990 ;
	
	List< List<Integer> > items = new ArrayList< List<Integer> >();
        List< Integer > tempList = new ArrayList< Integer >();
        int tempCount = 0;
        for( Integer id : list )
        {
            if( tempCount == bactchSize )
            {
                items.add( tempList );
                tempList = new ArrayList< Integer >();
                tempCount = 0;
            }
            
            tempList.add( id );
            tempCount++;
        }
        if( !tempList.isEmpty() )
        {
            items.add( tempList );
            tempList = new ArrayList< Integer >();
        }
                    
        return items;

    }
    public static List< List< Integer> > getInClauseList( List<Integer> list )
    {
	return getInClauseList(list,BATCH_SIZE ) ;
    }   
}
