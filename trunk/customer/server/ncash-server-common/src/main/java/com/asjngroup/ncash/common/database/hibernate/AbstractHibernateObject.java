package com.asjngroup.ncash.common.database.hibernate;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.asjngroup.ncash.common.database.helper.IdHelper;
import com.asjngroup.ncash.common.database.hibernate.audit.AuditAbstractHibernateObject;
import com.asjngroup.ncash.common.util.DateHelper;

public abstract class AbstractHibernateObject implements HibernateObject,Serializable
{
	private static final long serialVersionUID = 1L;

	// Fields common to all hibernate objects
	private int id = -1;

	private int versionId = 1;

	private Boolean deleteFl = false;

	private int partitionId = -1;

	private boolean isNew = false;

	private String auditInfo;

	private String abstractEntEntity;

	private int abstractAuditLevel;

	private transient AuditAbstractHibernateObject auditHibernateObject;

	// Accessors for all the common fields
	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		if ( id < 0 )
		{
			AbstractHibernateObject object = IdHelper.allocateId( this );
			id = object.getId();
		}
		this.id = id;
	}

	public void setCopiedId( int id )
	{
		this.id = id;
	}

	public int getVersionId()
	{
		return versionId;
	}

	public void setVersionId( int versionId )
	{
		this.versionId = versionId;
	}

	public Boolean getDeleteFl()
	{
		return deleteFl;
	}

	public void setDeleteFl( Boolean deleteFl )
	{
		this.deleteFl = deleteFl;
	}

	public int getPartitionId()
	{
		return partitionId;
	}

	public void setPartitionId( int partitionId )
	{
		this.partitionId = partitionId;
	}

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
	protected AbstractHibernateObject()
	{
		this( false );
	}

	protected AbstractHibernateObject( boolean generateId )
	{
		if ( generateId )
		{
			allocateId();
		}
	}

	public AuditAbstractHibernateObject getAuditHibernateObject()
	{
		return auditHibernateObject;
	}

	public void setAuditHibernateObject( AuditAbstractHibernateObject auditHibernateObject )
	{
		this.auditHibernateObject = auditHibernateObject;
	}

	public final void allocateId()
	{
		this.id = IdHelper.generateId( getClass() );
	}

	// Default implementations of standard object functions
	public String toString()
	{
		// Return the class name and the primary key
		return this.getClass().toString() + " " + getId();
	}

	public final boolean equals( Object other )
	{
		// Check for reference equality
		if ( this == other )
		{
			return true;
		}

		if ( other == null )
		{
			return false;
		}

		// Check the type is the same (this also checks for null)
		if ( !( other instanceof HibernateObject ) )
		{
			return false;
		}

		if ( !getClass().isInstance( other ) && !other.getClass().isInstance( this ) )
		{
			return false;
		}

		// Check only the primary keys
		HibernateObject otherHibernateObject = ( HibernateObject ) other;
		return this.getId() == otherHibernateObject.getId();
	}

	public final int hashCode()
	{
		// Base the hashcode on the primary key
		return getId();
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public void shallowCopy( AbstractHibernateObject hibObject )
	{
		hibObject.setCopiedId( IdHelper.getIdFor( this.getClass() ) );
		hibObject.setIsNew( true );
		hibObject.setPartitionId( getPartitionId() );
		hibObject.setVersionId( 1 );
		hibObject.setDeleteFl( getDeleteFl() );
	}

	public abstract AbstractHibernateObject shallowCopy( Map<AbstractHibernateObject, AbstractHibernateObject> map );

	public abstract void deepCopy( AbstractHibernateObject hibObj, Map<AbstractHibernateObject, AbstractHibernateObject> map, LinkedList<AbstractHibernateObject> queue );

	@SuppressWarnings( "unchecked" )
	public <T extends AbstractHibernateObject> T copyEntity()
	{
		Map<AbstractHibernateObject, AbstractHibernateObject> map = new HashMap<AbstractHibernateObject, AbstractHibernateObject>();
		return ( T ) copyEntity( map );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends AbstractHibernateObject> T copyEntity( Map<AbstractHibernateObject, AbstractHibernateObject> map )
	{
		LinkedList<AbstractHibernateObject> queue = new LinkedList<AbstractHibernateObject>();
		T _hibObj = ( T ) shallowCopy( map );
		map.put( this, _hibObj );
		deepCopy( _hibObj, map, queue );
		while ( queue.size() > 0 )
		{
			AbstractHibernateObject hibObject = queue.remove();
			AbstractHibernateObject copyObject = map.get( hibObject );
			hibObject.deepCopy( copyObject, map, queue );
		}
		return _hibObj;
	}

	public String getAuditingDisplayString()
	{
		return getDisplayString();
	}

	public String getDisplayString()
	{
		return null;
	}

	public void setIsNew( boolean isNew )
	{
		this.isNew = isNew;
	}

	public boolean getIsNew()
	{
		return isNew;
	}

	public boolean isNew()
	{
		return isNew;
	}

	public String getAuditInfo()
	{
		return auditInfo;
	}

	public void setAuditInfo( String auditInfo )
	{
		this.auditInfo = auditInfo;
	}

	public String getAbstractEntEntity()
	{
		return abstractEntEntity;
	}

	public void setAbstractEntEntity( String abstractEntEntity )
	{
		this.abstractEntEntity = abstractEntEntity;
	}

	public int getAbstractAuditLevel()
	{
		return abstractAuditLevel;
	}

	public void setAbstractAuditLevel( int abstractAuditLevel )
	{
		this.abstractAuditLevel = abstractAuditLevel;
	}
}
