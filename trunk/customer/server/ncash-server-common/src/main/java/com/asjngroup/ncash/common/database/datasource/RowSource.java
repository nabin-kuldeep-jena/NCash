package com.asjngroup.ncash.common.database.datasource;

public interface RowSource<T>
{
    public boolean next();

    public T get();

    public void beforeFirst();
}