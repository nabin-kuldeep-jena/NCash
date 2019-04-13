package com.asjngroup.ncash.common.hibernate.generate;

import com.asjngroup.ncash.common.util.StringHelper;

public class NestedObject implements Cloneable
{
	private FieldMapping fieldMapping;
	private EntityMapping foreignEntityMapping;

	public NestedObject()
	{
	}

	public NestedObject( FieldMapping paramPropertyMapping, EntityMapping paramObjectMapping )
	{
		this.fieldMapping = paramPropertyMapping;
		this.foreignEntityMapping = paramObjectMapping;
	}

	public FieldMapping getFieldMapping()
	{
		return this.fieldMapping;
	}

	public void setFieldMapping( FieldMapping paramPropertyMapping )
	{
		this.fieldMapping = paramPropertyMapping;
	}

	public EntityMapping getForeignEntityMapping()
	{
		return this.foreignEntityMapping;
	}

	public void setForeignObjectMapping( EntityMapping paramObjectMapping )
	{
		this.foreignEntityMapping = paramObjectMapping;
	}

	public String getFieldName()
	{
		if ( !( StringHelper.isEmpty( this.fieldMapping.getFieldInstanceName() ) ) )
			return StringHelper.lowerFirstChar( this.fieldMapping.getFieldInstanceName() );
		String str1 = SchemaHelper.getOneToManyRelationshipPrefix( this.fieldMapping.getFieldName(), this.fieldMapping.isPrefixRequired() );
		String str2 = str1 + getForeignEntityMapping().getEntityName();
		return StringHelper.lowerFirstChar( str2 );
	}

	public Object clone() throws CloneNotSupportedException
	{
		NestedObject localNestedObject = ( NestedObject ) super.clone();
		localNestedObject.setFieldMapping( ( FieldMapping ) this.fieldMapping.clone() );
		return localNestedObject;
	}
}