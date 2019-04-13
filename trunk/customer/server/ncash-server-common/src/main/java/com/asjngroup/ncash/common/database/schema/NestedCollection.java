package com.asjngroup.ncash.common.database.schema;

import java.io.IOException;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 10:12:82
 */
public class NestedCollection implements Cloneable
{
    public String PropertyName;
    public String ObjectName;
    public String ForeignKey;

    public transient EntityMapping entityMapping;

    public Object clone() throws CloneNotSupportedException
    {
        NestedCollection nestedCollection = new NestedCollection();

        nestedCollection.PropertyName = PropertyName;
        nestedCollection.ObjectName = ObjectName;
        nestedCollection.ForeignKey = ForeignKey;

        return nestedCollection;
    }
}
