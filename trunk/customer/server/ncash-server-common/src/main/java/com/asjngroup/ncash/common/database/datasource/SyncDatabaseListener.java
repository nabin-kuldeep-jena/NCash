package com.asjngroup.ncash.common.database.datasource;

import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Table;

public interface SyncDatabaseListener
{
    public boolean createTable( Table table );

    public boolean updateTable( Table fromTable, Table toTable );

    public boolean updateIndex( Index fromIndex, Index toIndex );

    public boolean dropTable( Table table );
}
