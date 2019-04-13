package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;

@Root( name = "entity", strict = false )
public class EntityMapping implements Cloneable
{

	@Attribute( name = "entityName" )
	private String entityName;

	@Attribute( name = "entityPrefix" )
	private String entityPrefix;

	@Attribute( name = "displayProperty", required = false )
	private String displayProperty;

	@Attribute( name = "displayString", required = false )
	private String displayString;

	@Attribute( name = "auditingDisplayString", required = false )
	private String auditingDisplayString;

	@ElementList( name = "fields", type = FieldMapping.class )
	private List<FieldMapping> fieldMappings = new ArrayList();

	@ElementList( name = "nestedCollections", type = NestedCollection.class )
	private List<NestedCollection> nestedCollections = new ArrayList();

	@ElementList( name = "constraints", type = Constraint.class )
	private List<Constraint> constraints = new ArrayList();

	public FieldMapping findFieldMapping( String paramString )
	{
		Iterator localIterator = this.fieldMappings.iterator();
		while ( localIterator.hasNext() )
		{
			FieldMapping localPropertyMapping = ( FieldMapping ) localIterator.next();
			if ( localPropertyMapping.getFieldName().equals( paramString ) )
				return localPropertyMapping;
		}
		return null;
	}

	public Constraint findConstraint( String paramString )
	{
		Iterator localIterator = this.constraints.iterator();
		while ( localIterator.hasNext() )
		{
			Constraint localConstraint = ( Constraint ) localIterator.next();
			if ( localConstraint.getConstraintSuffix().equals( paramString ) )
				return localConstraint;
		}
		return null;
	}

	public NestedCollection findNestedCollection( String paramString )
	{
		Iterator localIterator = this.nestedCollections.iterator();
		while ( localIterator.hasNext() )
		{
			NestedCollection localNestedCollection = ( NestedCollection ) localIterator.next();
			if ( localNestedCollection.getFieldName().equals( paramString ) )
				return localNestedCollection;
		}
		return null;
	}

	public FieldMapping getDisplayFieldMapping()
	{
		String str = getDisplayProperty();
		if ( StringHelper.isEmpty( str ) )
			return null;
		FieldMapping localPropertyMapping = findFieldMapping( str );
		if ( localPropertyMapping == null )
			throw new NCashRuntimeException( "The display property field for entity mapping %1 is invalid - the field mapping '%2' does not exist", new Object[]
			{ getEntityName(), str } );
		return localPropertyMapping;
	}

	public String getEntityName()
	{
		return this.entityName;
	}

	public void setEntityName( String paramString )
	{
		this.entityName = paramString;
	}

	public String getEntityPrefix()
	{
		return this.entityPrefix;
	}

	public void setEntityPrefix( String paramString )
	{
		this.entityPrefix = paramString;
	}

	public String getDisplayProperty()
	{
		return this.displayProperty;
	}

	public void setDisplayProperty( String paramString )
	{
		this.displayProperty = paramString;
	}

	public String getDisplayString()
	{
		return this.displayString;
	}

	public void setDisplayString( String paramString )
	{
		this.displayString = paramString;
	}

	public List<FieldMapping> getFieldMappings()
	{
		return this.fieldMappings;
	}

	public List<NestedCollection> getNestedCollections()
	{
		return this.nestedCollections;
	}

	public List<Constraint> getConstraints()
	{
		return this.constraints;
	}

	public void setConstraints( List<Constraint> paramList )
	{
		this.constraints = paramList;
	}

	public Constraint findBusinessConstraint()
	{
		Iterator localIterator = getConstraints().iterator();
		while ( localIterator.hasNext() )
		{
			Constraint localConstraint = ( Constraint ) localIterator.next();
			if ( localConstraint.isBusinessConstraint() )
				return localConstraint;
		}
		return null;
	}

	public Object clone() throws CloneNotSupportedException
	{
		EntityMapping localObjectMapping = ( EntityMapping ) super.clone();
		localObjectMapping.fieldMappings = new ArrayList();
		Iterator localIterator = getFieldMappings().iterator();
		Object localObject;
		while ( localIterator.hasNext() )
		{
			localObject = ( FieldMapping ) localIterator.next();
			localObjectMapping.getFieldMappings().add( ( FieldMapping ) ( ( FieldMapping ) localObject ).clone() );
		}
		localObjectMapping.nestedCollections = new ArrayList();
		localIterator = getNestedCollections().iterator();
		while ( localIterator.hasNext() )
		{
			localObject = ( NestedCollection ) localIterator.next();
			localObjectMapping.getNestedCollections().add( ( NestedCollection ) ( ( NestedCollection ) localObject ).clone() );
		}
		localObjectMapping.constraints = new ArrayList();
		localIterator = getConstraints().iterator();
		while ( localIterator.hasNext() )
		{
			localObject = ( Constraint ) localIterator.next();
			localObjectMapping.getConstraints().add( ( Constraint ) ( ( Constraint ) localObject ).clone() );
		}
		return localObjectMapping;
	}

	public boolean equals( Object paramObject )
	{
		if ( this == paramObject )
			return true;
		if ( ( paramObject == null ) || ( super.getClass() != paramObject.getClass() ) )
			return false;
		EntityMapping localObjectMapping = ( EntityMapping ) paramObject;
		if ( !( this.constraints.equals( localObjectMapping.constraints ) ) )
			return false;
		if ( !( this.nestedCollections.equals( localObjectMapping.nestedCollections ) ) )
			return false;
		if ( !( this.fieldMappings.equals( localObjectMapping.fieldMappings ) ) )
			return false;
		if ( !( this.entityName.equals( localObjectMapping.entityName ) ) )
			return false;
		if ( !( this.entityPrefix.equals( localObjectMapping.entityPrefix ) ) )
			return false;
		if ( !( this.displayProperty.equals( localObjectMapping.displayProperty ) ) )
			return false;
		return ( this.displayString.equals( localObjectMapping.displayString ) );
	}

	public int hashCode()
	{
		int i = this.entityName.hashCode();
		i = 31 * i + this.entityPrefix.hashCode();
		return i;
	}

	public void setAuditingDisplayString( String paramString )
	{
		this.auditingDisplayString = paramString;
	}

	public String getAuditingDisplayString()
	{
		return this.auditingDisplayString;
	}

	public Table getTable()
	{
		Table localTable = new Table();
		localTable.setTableName( SchemaHelper.entityNameToDatabaseName( getEntityName() ) );
		localTable.setTablePrefix( SchemaHelper.entityNameToDatabaseName( getEntityPrefix() ) );
		Iterator localIterator = getFieldMappings().iterator();
		Object localObject;
		while ( localIterator.hasNext() )
		{
			localObject = ( FieldMapping ) localIterator.next();
			localTable.getColumns().add( ( ( FieldMapping ) localObject ).getColumn() );
		}
		localIterator = getConstraints().iterator();
		while ( localIterator.hasNext() )
		{
			localObject = ( Constraint ) localIterator.next();
			localTable.getIndexes().add( ( ( Constraint ) localObject ).getIndex( this ) );
		}
		return ( ( Table ) localTable );
	}
}