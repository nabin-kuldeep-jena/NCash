package com.asjngroup.ncash.common.util.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryObjectStorer<T> implements ObjectStorer< T >
{
    private List< T > storage = new ArrayList< T >();

    public void add( T t )
    {
        storage.add( t );
    }

    public IteratorEx< T > iterator()
    {
        return new IteratorImpl();
    }

    public void close()
    {
        // nothing to do
    }

    private class IteratorImpl implements IteratorEx< T >
    {
        private Iterator< T > storageIterator;

        public IteratorImpl()
        {
            storageIterator = storage.iterator();
        }

        public boolean hasNext() throws Exception
        {
            return storageIterator.hasNext();
        }

        public T next() throws Exception
        {
            return storageIterator.next();
        }
    }
}
