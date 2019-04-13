package com.asjngroup.ncash.common.database.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * User: quilleashm
 * Date: 25-May-2004
 * Time: 16:04:02
 */
public class Relationship
{
    public String ParentTableName;
    public String ParentDatabaseName;
    public String ChildTableName;
    public String ChildDatabaseName;

    public List< ColumnPair > ColumnPairs = new ArrayList< ColumnPair >();

    public Object clone() throws CloneNotSupportedException
    {
        Relationship relationship = new Relationship();

        relationship.ParentTableName = ParentTableName;
        relationship.ParentDatabaseName = ParentDatabaseName;
        relationship.ChildTableName = ChildTableName;
        relationship.ChildDatabaseName = ChildDatabaseName;

        for ( ColumnPair columnPair : ColumnPairs )
        {
            relationship.ColumnPairs.add( (ColumnPair)columnPair.clone() );
        }

        return relationship;
    }
}
