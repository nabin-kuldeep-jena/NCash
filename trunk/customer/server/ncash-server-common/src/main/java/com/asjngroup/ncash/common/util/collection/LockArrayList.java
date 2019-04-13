package com.asjngroup.ncash.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;

public class LockArrayList<E> extends BaseLockList< E >
{
    public LockArrayList()
    {
        super( new ArrayList< E >() );
    }

    public LockArrayList( Collection< ? extends E > c )
    {
        super( new ArrayList< E >( c ) );
    }
}
