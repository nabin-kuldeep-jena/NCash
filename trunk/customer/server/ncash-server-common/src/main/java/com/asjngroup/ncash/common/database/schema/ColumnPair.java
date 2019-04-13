package com.asjngroup.ncash.common.database.schema;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 19:11:33
 */
public class ColumnPair
{
    public String ParentColumnName;
    public String ChildColumnName;

    public Object clone() throws CloneNotSupportedException
    {
        ColumnPair columnPair = new ColumnPair();

        columnPair.ParentColumnName = ParentColumnName;
        columnPair.ChildColumnName = ChildColumnName;

        return columnPair;
    }
}
