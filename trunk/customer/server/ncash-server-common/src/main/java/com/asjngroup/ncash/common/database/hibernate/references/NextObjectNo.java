package com.asjngroup.ncash.common.database.hibernate.references;


import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.LinkedList;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.asjngroup.ncash.common.database.hibernate.AbstractHibernateObject;

@Entity
@javax.persistence.Table( name = "next_object_no" )
@Filters(
{ @org.hibernate.annotations.Filter( name = "partitionFilter" ), @org.hibernate.annotations.Filter( name = "deletedFilter", condition = "delete_fl = :deleteFl" ) } )
public class NextObjectNo extends AbstractHibernateObject
{
	private static final long serialVersionUID = 1L;
	private String nonObjectName;
	private Long nonCurrentNo;

	public NextObjectNo()
	{
		this( false );
	}

	public NextObjectNo( boolean generateId )
	{
		super( generateId );

		this.nonObjectName = null;
		this.nonCurrentNo = null;
	}

	@Id
	@Column( name = "non_id" )
	@Type( type = "int" )
	@Min( 1L )
	public int getNoiId()
	{
		return getId();
	}

	public void setNoiId( int id )
	{
		setId( id );
	}

	@Version
	@Column( name = "version_id" )
	@Type( type = "int" )
	public int getVersionId()
	{
		return super.getVersionId();
	}

	@Basic
	@Column( name = "delete_fl" )
	@Type( type = "yes_no" )
	@NotNull
	public Boolean getDeleteFl()
	{
		return super.getDeleteFl();
	}

	@Basic
	@Column( name = "ptn_id" )
	@Type( type = "int" )
	@Min( 0L )
	public int getPartitionId()
	{
		return super.getPartitionId();
	}

	@Basic
	@Column( name = "non_object_name", nullable = false, insertable = true, updatable = true )
	@Type( type = "org.hibernate.type.StringType" )
	@NotNull
	@NotEmpty
	@Length( max = 255 )
	public String getNonObjectName()
	{
		return this.nonObjectName;
	}

	public void setNonObjectName( String val )
	{
		this.nonObjectName = val;
	}

	@Basic
	@Column( name = "non_current_no", nullable = false, insertable = true, updatable = true )
	@Type( type = "long" )
	@NotNull
	public Long getNonCurrentNo()
	{
		return this.nonCurrentNo;
	}

	public void setNonCurrentNo( Long val )
	{
		this.nonCurrentNo = val;
	}

	@Transient
	public String getSimpleDisplayString()
	{
		return null;
	}

	@Transient
	public String getDisplayString()
	{
		return super.getDisplayString();
	}

	@Transient
	public String getAuditingDisplayString()
	{
		return super.getAuditingDisplayString();
	}

	@Transient
	public String getDisplayValue()
	{
		return null;
	}

	public NextObjectNo shallowCopy( Map<AbstractHibernateObject, AbstractHibernateObject> map )
	{
		NextObjectNo __nextObjectNo = new NextObjectNo();
		super.shallowCopy( __nextObjectNo );
		__nextObjectNo.setNonObjectName( getNonObjectName() );
		__nextObjectNo.setNonCurrentNo( getNonCurrentNo() );
		return __nextObjectNo;
	}

	public void deepCopy( AbstractHibernateObject _entity, Map<AbstractHibernateObject, AbstractHibernateObject> map, LinkedList<AbstractHibernateObject> queue )
	{
		NextObjectNo __nextObjectNo = ( NextObjectNo ) _entity;
	}
}
