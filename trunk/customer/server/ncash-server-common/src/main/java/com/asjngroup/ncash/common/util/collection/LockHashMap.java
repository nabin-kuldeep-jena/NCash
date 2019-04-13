package com.asjngroup.ncash.common.util.collection;

import java.util.HashMap;
import java.util.Map;

public class LockHashMap<K, V> extends BaseLockMap< K, V >
{
    public LockHashMap()
    {
        super( new HashMap< K, V >() );
    }

    public LockHashMap( Map< ? extends K, ? extends V > c )
    {
        super( new HashMap< K, V >( c ) );
    }
}
