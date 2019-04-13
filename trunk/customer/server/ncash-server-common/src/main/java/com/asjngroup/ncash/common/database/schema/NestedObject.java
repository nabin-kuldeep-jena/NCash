package com.asjngroup.ncash.common.database.schema;

/**
 * User: nabin.jena
 * Date: 07-Apr-2017
 * Time: 10:18:26
 */


public class NestedObject implements Cloneable
{
    public String PropertyName;
    public String ObjectName;
    public String ForeignKey;
    public String ServerPackage;

    public transient EntityMapping entityMapping;

    public Object clone() throws CloneNotSupportedException
    {
        NestedObject nestedObject = new NestedObject();

        nestedObject.PropertyName = PropertyName;
        nestedObject.ObjectName = ObjectName;
        nestedObject.ForeignKey = ForeignKey;
        nestedObject.ServerPackage = ServerPackage;

        return nestedObject;
    }
}
