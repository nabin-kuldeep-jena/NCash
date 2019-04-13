package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;

@Root( name = "constraint" )
public class Constraint implements Cloneable
{

	@Attribute( name = "constraintSuffix" )
	private String constraintSuffix;

	@Attribute( name = "isUnique" )
	private boolean unique;

	@Attribute( name = "isClustered" )
	private boolean clustered;

	@Attribute( name = "isBusinessConstraint" )
	private boolean businessConstraint;

	@Attribute( name = "isDisplayName" )
	private boolean displayName;

	@Attribute( name = "propertyList" )
	private String propertyList;

	public Constraint()
	{
	}

	public Constraint( String paramString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4 )
	{
		this.constraintSuffix = paramString;
		this.unique = paramBoolean1;
		this.clustered = paramBoolean2;
		this.businessConstraint = paramBoolean3;
		this.displayName = paramBoolean4;
	}

	public void addProperty( String paramString )
	{
		if ( StringHelper.isEmpty( this.propertyList ) )
		{
			this.propertyList = paramString;
		}
		else
		{
			Constraint tmp26_25 = this;
			tmp26_25.propertyList = tmp26_25.propertyList + "," + paramString;
		}
	}

	public boolean equals( Object paramObject )
	{
		if ( paramObject == null )
			return false;
		if ( !( paramObject instanceof Constraint ) )
			return false;
		Constraint localConstraint = ( Constraint ) paramObject;
		if ( !( this.constraintSuffix.equalsIgnoreCase( localConstraint.constraintSuffix ) ) )
			return false;
		if ( this.unique != localConstraint.unique )
			return false;
		if ( this.clustered != localConstraint.clustered )
			return false;
		return ( this.propertyList.equals( localConstraint.propertyList ) );
	}

	public String getConstraintSuffix()
	{
		return this.constraintSuffix;
	}

	public void setConstraintSuffix( String paramString )
	{
		this.constraintSuffix = paramString;
	}

	public boolean isUnique()
	{
		return this.unique;
	}

	public void setUnique( boolean paramBoolean )
	{
		this.unique = paramBoolean;
	}

	public boolean isClustered()
	{
		return this.clustered;
	}

	public void setClustered( boolean paramBoolean )
	{
		this.clustered = paramBoolean;
	}

	public boolean isBusinessConstraint()
	{
		return this.businessConstraint;
	}

	public void setBusinessConstraint( boolean paramBoolean )
	{
		this.businessConstraint = paramBoolean;
	}

	public boolean isDisplayName()
	{
		return this.displayName;
	}

	public void setDisplayName( boolean paramBoolean )
	{
		this.displayName = paramBoolean;
	}

	public String getPropertyList()
	{
		return this.propertyList;
	}

	public void setPropertyList( String paramString )
	{
		this.propertyList = paramString;
	}

	public List<EntityMapping> getEntityMappings( EntityMapping paramObjectMapping )
	{
		ArrayList localArrayList = new ArrayList();
		String[] arrayOfString1 = StringHelper.split( getPropertyList(), "," );
		for ( String str : arrayOfString1 )
		{
			FieldMapping localPropertyMapping = paramObjectMapping.findFieldMapping( str );
			if ( localPropertyMapping == null )
				throw new NCashRuntimeException( "The constraint %1 on object mapping %2 is invalid - the property '%3' is not in the object mapping", new Object[]
				{ getConstraintSuffix(), paramObjectMapping.getEntityName(), str } );
			localArrayList.add( localPropertyMapping );
		}
		return localArrayList;
	}

	public Index getIndex( EntityMapping paramObjectMapping )
	{
		Index localIndex = new Index();
		localIndex.setBusinessConstraint( isBusinessConstraint() );
		localIndex.setUnique( isUnique() );
		localIndex.setDisplayName( isDisplayName() );
		localIndex.setIndexName( SchemaHelper.entityNameToDatabaseName( paramObjectMapping.getEntityName() + "_" + getConstraintSuffix() ) );
		localIndex.setColumnList( SchemaHelper.entityNameToDatabaseName( getPropertyList() ) );
		return localIndex;
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}