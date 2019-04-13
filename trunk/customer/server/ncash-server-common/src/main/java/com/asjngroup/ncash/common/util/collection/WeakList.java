package com.asjngroup.ncash.common.util.collection;

import java.util.Iterator;
import java.util.AbstractList;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

public class WeakList<T> extends AbstractList< T >
{
    private List< WeakReference< T > > refs;

    // allows the caller to instantiate their own list inside here for different implementations 
    public WeakList( List< WeakReference< T >> refs )
    {
        this.refs = refs;
    }

    public WeakList()
    {
        this( new ArrayList< WeakReference< T > >() );
    }

    public T get( int index )
    {
        return refs.get( index ).get();
    }

    public boolean add( T t )
    {
        return refs.add( new WeakReference< T >( t ) );
    }

    public boolean remove( Object t )
    {
        for ( Iterator< WeakReference< T >> iterator = refs.iterator(); iterator.hasNext(); )
        {
            WeakReference< T > weakReference = iterator.next();

            if ( weakReference.get().equals( t ) )
            {
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public int size()
    {
        return refs.size();
    }
}
