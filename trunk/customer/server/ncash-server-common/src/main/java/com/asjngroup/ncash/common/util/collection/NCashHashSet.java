package com.asjngroup.ncash.common.util.collection;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Arrays;

public class NCashHashSet< E > extends AbstractSet< E >
{
    private SetEntry[] entries;
    private int size;
    private int threshold;
    private float loadFactor = 0.75f;
    static final Object NULL_KEY = new Object();

    // pinched from HashMap
    static int hash( Object x )
    {

        int h;
        if ( x instanceof byte[] )
        {
            h = Arrays.hashCode( (byte[])x );
        }
        else
        {
            h = x.hashCode();
        }

        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        h ^= (h >>> 10);
        return h;
    }

    /**
     * Check for equality of non-null reference x and possibly-null y.
     */
    static boolean eq( Object x, Object y )
    {
        if ( x instanceof byte[] )
        {
            return Arrays.equals( (byte[])x, (byte[])y );
        }
        return x == y || x.equals( y );
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor( int h, int length )
    {
        return h & (length - 1);
    }

    public NCashHashSet()
    {
        entries = new SetEntry[16];

        clear();
    }

    public int size()
    {
        return size;
    }

    public boolean contains( Object o )
    {
        o = maskNull( o );
        int hashCode = hash( o );
        int index = indexFor( hashCode, entries.length );

        SetEntry< E > entry = entries[index];

        while ( true )
        {
            if ( entry == null )
                return false;
            if ( entry.hash == hashCode && eq( o, entry.key ) )
                return true;
            entry = entry.nextEntry;
        }
    }

    public Iterator< E > iterator()
    {
        return new SparkHashSetIterator();
    }

    public boolean add( E e )
    {
        e = maskNull( e );
        int hashCode = hash( e );
        int index = indexFor( hashCode, entries.length );

        for ( SetEntry< E > entry = entries[index]; entry != null; entry = entry.nextEntry )
        {
            if ( entry.hash == hashCode && eq( e, entry.key ) )
            {
                return false;
            }
        }

        SetEntry< E > newEntry = new SetEntry< E >();
        newEntry = new SetEntry< E >();
        newEntry.hash = hashCode;
        newEntry.key = e;
        newEntry.nextEntry = entries[index];

        entries[index] = newEntry;

        if ( size++ >= threshold )
        {
            resize( 2 * entries.length );
        }

        return true;
    }

    public boolean remove( Object o )
    {
        o = maskNull( o );
        int hashCode = hash( o );
        int index = indexFor( hashCode, entries.length );

        SetEntry prevEntry = null;
        for ( SetEntry< E > entry = entries[index]; entry != null; entry = entry.nextEntry )
        {
            if ( entry.hash == hashCode && eq( o, entry.key ) )
            {
                if ( prevEntry == null )
                {
                    entries[index] = entry.nextEntry;
                }
                else
                {
                    prevEntry.nextEntry = entry.nextEntry;
                }

                size--;

                return true;
            }
            else
            {
                prevEntry = entry;
            }
        }

        return false;
    }

    void resize( int newCapacity )
    {
        SetEntry[] oldTable = entries;
        int oldCapacity = oldTable.length;

        // if ( oldCapacity == MAXIMUM_CAPACITY )
        // {
        // threshold = Integer.MAX_VALUE;
        // return;
        // }

        SetEntry[] newTable = new SetEntry[newCapacity];
        transfer( newTable );
        entries = newTable;
        threshold = (int)(newCapacity * loadFactor);
    }

    /**
     * Transfer all entries from current table to newTable.
     */
    void transfer( SetEntry[] newTable )
    {
        SetEntry[] src = entries;
        int newCapacity = newTable.length;
        for ( int j = 0; j < src.length; j++ )
        {
            SetEntry< E > e = src[j];
            if ( e != null )
            {
                src[j] = null;
                do
                {
                    SetEntry< E > next = e.nextEntry;
                    int i = indexFor( e.hash, newCapacity );
                    e.nextEntry = newTable[i];
                    newTable[i] = e;
                    e = next;
                }
                while ( e != null );
            }
        }
    }

    public void clear()
    {
        Arrays.fill( entries, null );
        size = 0;
        threshold = (int)(entries.length * loadFactor);
    }
    
    static <T> T maskNull(T key) 
    {
        return key == null ? (T)NULL_KEY : key;
    }
    
    static <T> T unmaskNull(T key)
    {
        return (key == NULL_KEY ? null : key);
    }
    
    static class SetEntry< E >
    {
        E key;
        SetEntry nextEntry;
        int hash;
    }

    class SparkHashSetIterator implements Iterator< E >
    {
        private SetEntry next;
        private int index;

        public SparkHashSetIterator()
        {
            index = 0;

            findNextEntry();
        }

        private void findNextEntry()
        {
            if ( next != null )
            {
                if ( next.nextEntry != null )
                {
                    next = next.nextEntry;
                    return;
                }
                else
                {
                    next = null;
                }
            }

            while ( next == null && index < entries.length )
            {
                next = entries[index];
                index++;
            }
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public E next()
        {
            E ret = (E)next.key;

            findNextEntry();

            return unmaskNull( ret );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
