package com.asjngroup.ncash.common.util.collection;

import java.util.Map;
import java.util.concurrent.locks.Lock;

public interface LockMap<K, V> extends Map< K, V >
{
    public Lock readLock();

    public Lock writeLock();
}
