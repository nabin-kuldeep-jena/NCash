package com.asjngroup.ncash.common.database.hibernate;

import java.io.Serializable;

import com.asjngroup.ncash.common.database.hibernate.audit.AuditAbstractHibernateObject;

public interface HibernateObject extends Cloneable, Serializable
{
    // Fields common to all hibernate objects
    public int getId();
    public void setId(int id);
    public int getVersionId();
    public void setVersionId(int versionId);
    public Boolean getDeleteFl();
    public void setDeleteFl(Boolean deleteFl);
    public int getPartitionId();
    public void setPartitionId(int partitionId);
    public void setAuditHibernateObject(AuditAbstractHibernateObject auditObject);
    public AuditAbstractHibernateObject getAuditHibernateObject ();
    
    public Object clone() throws CloneNotSupportedException;


    /**
     * This returns one of the properties of this hibernate object that describes it
     * most accurately. As an example the hibernate object may return its name or
     * description field. A display string may not be specified in the schema file, so
     * this method can return null. This method may not return unique values for all
     * hibernate objects of this class. For a unique display string see the
     * <code>getDisplayString()</code> method.
     * @return A string that describes this hibernate object, or null if no display string
     * is specified in the schema file.
     */
    public String getSimpleDisplayString();

    /**
     * This returns a internationalized string that can be used to describe this hibernate object. The schema
     * file defines some of the property mappings in this object that are used to construct
     * the display string with optional separators between the properties. If one of the properties
     * chosen is a nested object then the display name of that nested object will be used (not
     * the ID). The schema guarantees that this string will be unique for a given hibernate
     * object. The display string is not mandatory, so a call to this method can return null.
     * @return A String that describes this object.
     */
    public String getDisplayString();
    
    /**
     * This returns a string that can be used to describe this hibernate object. The schema
     * file defines some of the property mappings in this object that are used to construct
     * the display string with optional separators between the properties. If one of the properties
     * chosen is a nested object then the display name of that nested object will be used (not
     * the ID). The schema guarantees that this string will be unique for a given hibernate
     * object. The display string is not mandatory, so a call to this method can return null.
     * @return A String that describes this object.
     */
    public String getDisplayValue();
}
