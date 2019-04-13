package com.asjngroup.ncash.common.hibernate.generate;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import com.asjngroup.ncash.common.database.hibernate.ColumnDataType;
import com.asjngroup.ncash.common.util.StringHelper;

@Root( name = "field", strict = false )
public class FieldMapping implements Cloneable
{

	@Attribute( name = "fieldName" )
	private String fieldName;

	@Attribute( name = "datatype" )
	private ColumnDataType datatype;

	@Attribute( name = "isMandatory" )
	private boolean mandatory;

	@Attribute( name = "isOneToOneNestedObject", required = false )
	private boolean oneToOneNestedObject;

	@Attribute( name = "fieldInstanceName", required = false )
	private String fieldInstanceName;

	@Attribute( name = "noCopy", required = false )
	private boolean noCopy = false;

	@Attribute( name = "refCopy", required = false )
	private boolean refCopy = false;

	@Attribute( name = "deepCopy", required = false )
	private boolean deepCopy = false;

	@Attribute( name = "loadNestedObject", required = false )
	private boolean loadNestedObject = true;

	@Attribute( name = "maxLength", required = false )
	private int maxLength = -1;

	@Attribute( name = "isForeignKey", required = false )
	private boolean foreignKey = true;

	@Attribute( name = "prefixRequired", required = false )
	private boolean prefixRequired = false;

	public String getFieldName()
	{
		return this.fieldName;
	}

	public void setFieldName( String paramString )
	{
		this.fieldName = paramString;
	}

	public ColumnDataType getDatatype()
	{
		return this.datatype;
	}

	public void setDatatype( ColumnDataType paramSparkType )
	{
		this.datatype = paramSparkType;
	}

	public boolean isMandatory()
	{
		return this.mandatory;
	}

	public void setMandatory( boolean paramBoolean )
	{
		this.mandatory = paramBoolean;
	}

	public boolean isOneToOneNestedObject()
	{
		return this.oneToOneNestedObject;
	}

	public void setOneToOneNestedObject( boolean paramBoolean )
	{
		this.oneToOneNestedObject = paramBoolean;
	}

	public String getFieldInstanceName()
	{
		return this.fieldInstanceName;
	}

	public void setFieldInstanceName( String paramString )
	{
		this.fieldInstanceName = paramString;
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public boolean equals( Object paramObject )
	{
		if ( this == paramObject )
			return true;
		if ( ( paramObject == null ) || ( super.getClass() != paramObject.getClass() ) )
			return false;
		FieldMapping localPropertyMapping = ( FieldMapping ) paramObject;
		if ( this.mandatory != localPropertyMapping.mandatory )
			return false;
		if ( this.datatype != localPropertyMapping.datatype )
			return false;
		return ( this.fieldName.equals( localPropertyMapping.fieldName ) );
	}

	public int hashCode()
	{
		return this.fieldName.hashCode();
	}

	public void setNoCopy( boolean paramBoolean )
	{
		this.noCopy = paramBoolean;
	}

	public boolean isNoCopy()
	{
		return this.noCopy;
	}

	public void setRefCopy( boolean paramBoolean )
	{
		this.refCopy = paramBoolean;
	}

	public boolean isRefCopy()
	{
		return this.refCopy;
	}

	public void setDeepCopy( boolean paramBoolean )
	{
		this.deepCopy = paramBoolean;
	}

	public boolean isDeepCopy()
	{
		return this.deepCopy;
	}

	public void setLoadNestedObject( boolean paramBoolean )
	{
		this.loadNestedObject = paramBoolean;
	}

	public boolean isLoadNestedObject()
	{
		return this.loadNestedObject;
	}

	public void setMaxLength( int paramInt )
	{
		this.maxLength = paramInt;
	}

	public int getMaxLength()
	{
		return this.maxLength;
	}

	public void setForeignKey( boolean paramBoolean )
	{
		this.foreignKey = paramBoolean;
	}

	public boolean isForeignKey()
	{
		return this.foreignKey;
	}

	public void setPrefixRequired( boolean paramBoolean )
	{
		this.prefixRequired = paramBoolean;
	}

	public boolean isPrefixRequired()
	{
		return this.prefixRequired;
	}

	public Column getColumn()
	{
		Column localColumn = new Column();
		localColumn.setColumnName( StringHelper.camelCaseToUnderScore( getFieldName() ) );
		localColumn.setDataType( getDatatype() );
		localColumn.setMandatory( isMandatory() );
		if ( getMaxLength() == -1 )
		{
			localColumn.setLength( 0 );
			if ( getDatatype() == ColumnDataType.String )
				localColumn.setLength( 255 );
		}
		else
		{
			localColumn.setLength( getMaxLength() );
		}
		return localColumn;
	}
}
