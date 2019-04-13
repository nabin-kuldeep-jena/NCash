package com.asjngroup.ncash.common.hibernate.generate;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "nestedCollection")
public class NestedCollection implements Cloneable {

	@Attribute(name = "fieldName")
	private String fieldName;

	@Attribute(name = "foreignEntityMappingName")
	private String foreignEntityMappingName;

	@Attribute(name = "foreignFieldName")
	private String foreignFieldName;

	@Attribute(name = "orderingFieldName", required = false)
	private String orderingFieldName;

	@Attribute(name = "noCopy", required = false)
	private boolean noCopy = false;

	@Attribute(name = "refCopy", required = false)
	private boolean refCopy = false;

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String paramString) {
		this.fieldName = paramString;
	}

	public String getForeignEntityMappingName() {
		return this.foreignEntityMappingName;
	}

	public void setForeignEntityMappingName(String paramString) {
		this.foreignEntityMappingName = paramString;
	}

	public String getForeignFieldName() {
		return this.foreignFieldName;
	}

	public void setForeignFieldName(String paramString) {
		this.foreignFieldName = paramString;
	}

	public String getOrderingFieldName() {
		return this.orderingFieldName;
	}

	public void setOrderingFieldName(String paramString) {
		this.orderingFieldName = paramString;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setNoCopy(boolean paramBoolean) {
		this.noCopy = paramBoolean;
	}

	public boolean isNoCopy() {
		return this.noCopy;
	}

	public void setRefCopy(boolean paramBoolean) {
		this.refCopy = paramBoolean;
	}

	public boolean isRefCopy() {
		return this.refCopy;
	}
}