package com.asjngroup.ncash.common.util.collection;

import java.util.Iterator;
import java.util.Arrays;
import java.util.List;

public class IteratorCascaderEx<T> implements IteratorEx< T >
{
    // iterators to cascade in order
    private List< IteratorEx< T > > iterators;

    // iterator of iterators :)
    private Iterator< IteratorEx< T > > iteratorIterator;

    // current iterator
    private IteratorEx< T > currentIterator = null;

    public IteratorCascaderEx( IteratorEx< T > iterator )
    {
        this( Arrays.asList( iterator ) );
    }

    public IteratorCascaderEx( List< IteratorEx< T > > iterators )
    {
        this.iterators = iterators;

        // create the iterator iterator
        iteratorIterator = iterators.iterator();

        // initialise with the first iterator
        if ( iteratorIterator.hasNext() )
        {
            currentIterator = iteratorIterator.next();
        }
    }

    public boolean hasNext() throws Exception
    {
        // if the current iterator is null, then no elements available
        if ( currentIterator == null )
            return false;

        // if the current iterator has something we're ok
        if ( currentIterator.hasNext() )
            return true;

        // current iterator is empty so check the next iterator in the list
        if ( iteratorIterator.hasNext() )
        {
            // move to next iterator in list
            currentIterator = iteratorIterator.next();

            // recheck with the new current iterator
            return hasNext();
        }

        // out of iterators so no more to do
        return false;
    }

    public T next() throws Exception
    {
        // if the current iterator is empty move to the next one
        if ( !currentIterator.hasNext() )
        {
            // move to next iterator in list
            // will throw the NoSuchElementException if it has run out
            currentIterator = iteratorIterator.next();

            // recheck with the new current iterator
            return next();
        }

        return currentIterator.next();
    }
}
