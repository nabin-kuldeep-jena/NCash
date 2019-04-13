package com.asjngroup.ncash.common.util.collection;

import java.util.Arrays;

public class MultiKey
{
    private Object[] key;

    public MultiKey( Object... key )
    {
        this.key = key;
    }

    public Object get( int index )
    {
        return key[ index ];
    }

    public Object[] getKey()
    {
        return key;
    }

    public int hashCode()
    {
        return Arrays.hashCode( key );
    }

    public boolean equals( Object obj )
    {
        if ( obj == null )
            return false;
        if ( !( obj instanceof MultiKey ) )
            return false;

        MultiKey from = (MultiKey)obj;

        return Arrays.equals( key, from.key );
    }
}
