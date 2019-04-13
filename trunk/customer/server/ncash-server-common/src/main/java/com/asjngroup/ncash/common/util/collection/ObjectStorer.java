package com.asjngroup.ncash.common.util.collection;

public interface ObjectStorer<T>
{
    public void add( T obj ) throws ObjectStorerException;

    public IteratorEx< T > iterator() throws ObjectStorerException;

    public void close() throws ObjectStorerException;
}
