package com.asjngroup.ncash.common.util.collection;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseLockList<E> implements LockList< E >
{
    private List< E > internalList;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    BaseLockList( List< E > c )
    {
        internalList = c;
    }

    public E get( int index )
    {
        lock.readLock().lock();
        try
        {
            return internalList.get( index );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public E set( int index, E e )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.set( index, e );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void add( int index, E e )
    {
        lock.writeLock().lock();
        try
        {
            internalList.add( index, e );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public E remove( int index )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.remove( index );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public int indexOf( Object o )
    {
        lock.readLock().lock();
        try
        {
            return internalList.indexOf( o );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public int lastIndexOf( Object o )
    {
        lock.readLock().lock();
        try
        {
            return internalList.lastIndexOf( o );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public ListIterator< E > listIterator()
    {
        return internalList.listIterator();
    }

    public ListIterator< E > listIterator( int index )
    {
        return internalList.listIterator( index );
    }

    public List< E > subList( int fromIndex, int toIndex )
    {
        throw new UnsupportedOperationException();
    }

    public int size()
    {
        lock.readLock().lock();
        try
        {
            return internalList.size();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty()
    {
        lock.readLock().lock();
        try
        {
            return internalList.isEmpty();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean contains( Object o )
    {
        lock.readLock().lock();
        try
        {
            return internalList.contains( o );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public Iterator< E > iterator()
    {
        // relies on the user locking the list manually
        return internalList.iterator();
    }

    public Object[] toArray()
    {
        lock.readLock().lock();
        try
        {
            return internalList.toArray();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public <T> T[] toArray( T[] ts )
    {
        lock.readLock().lock();
        try
        {
            return internalList.toArray( ts );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean add( E e )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.add( e );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean remove( Object o )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.remove( o );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean containsAll( Collection< ? > c )
    {
        lock.readLock().lock();
        try
        {
            return internalList.containsAll( c );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean addAll( Collection< ? extends E > es )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.addAll( es );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean addAll( int index, Collection< ? extends E > es )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.addAll( index, es );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean removeAll( Collection< ? > c )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.removeAll( c );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean retainAll( Collection< ? > c )
    {
        lock.writeLock().lock();
        try
        {
            return internalList.retainAll( c );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void clear()
    {
        lock.writeLock().lock();
        try
        {
            internalList.clear();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public Lock readLock()
    {
        return lock.readLock();
    }

    public Lock writeLock()
    {
        return lock.writeLock();
    }
}
