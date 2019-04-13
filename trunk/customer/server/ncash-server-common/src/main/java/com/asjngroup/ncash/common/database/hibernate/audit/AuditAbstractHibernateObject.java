package com.asjngroup.ncash.common.database.hibernate.audit;


import org.joda.time.DateTime;

import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;
import com.asjngroup.ncash.common.util.DateHelper;

public abstract class AuditAbstractHibernateObject extends AbstractHibernateObject 
{
	private static final long serialVersionUID = 1L;
	private DateTime createdDttm = DateHelper.getMaximumDate();
    private int createdUsrId = 0;
    private DateTime modifiedDttm = DateHelper.getMaximumDate();
    private int modifiedUsrId = 0;

    public DateTime getCreatedDttm()
    {
        return createdDttm;
    }

    public void setCreatedDttm( DateTime createdDttm )
    {
        this.createdDttm = createdDttm;
    }

    public int getCreatedUsrId()
    {
        return createdUsrId;
    }

    public void setCreatedUsrId( int createdUsrId )
    {
        this.createdUsrId = createdUsrId;
    }

    public DateTime getModifiedDttm()
    {
        return modifiedDttm;
    }

    public void setModifiedDttm( DateTime modifiedDttm )
    {
        this.modifiedDttm = modifiedDttm;
    }

    public int getModifiedUsrId()
    {
        return modifiedUsrId;
    }

    public void setModifiedUsrId( int modifiedUsrId )
    {
        this.modifiedUsrId = modifiedUsrId;
    }

    // Default constructor
    public AuditAbstractHibernateObject()
    {
    }

   
}