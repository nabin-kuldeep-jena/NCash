package com.asjngroup.ncash.common.util.collection;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

public class BaseLockMap<K, V> implements LockMap< K, V >
{
    private Map< K, V > internalMap;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    BaseLockMap( Map< K, V > c )
    {
        this.internalMap = c;
    }

    public Lock readLock()
    {
        return lock.readLock();
    }

    public Lock writeLock()
    {
        return lock.writeLock();
    }

    public int size()
    {
        lock.readLock().lock();
        try
        {
            return internalMap.size();
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
            return internalMap.isEmpty();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey( Object key )
    {
        lock.readLock().lock();
        try
        {
            return internalMap.containsKey( key );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean containsValue( Object value )
    {
        lock.readLock().lock();
        try
        {
            return internalMap.containsValue( value );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public V get( Object key )
    {
        lock.readLock().lock();
        try
        {
            return internalMap.get( key );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public V put( K k, V v )
    {
        lock.writeLock().lock();
        try
        {
            return internalMap.put( k, v );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public V remove( Object key )
    {
        lock.writeLock().lock();
        try
        {
            return internalMap.remove( key );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void putAll( Map< ? extends K, ? extends V > map )
    {
        lock.writeLock().lock();
        try
        {
            internalMap.putAll( map );
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
            internalMap.clear();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public Set< K > keySet()
    {
        return internalMap.keySet();
    }

    public Collection< V > values()
    {
        return internalMap.values();
    }

    public Set< Entry< K, V >> entrySet()
    {
        return internalMap.entrySet();
    }
}
