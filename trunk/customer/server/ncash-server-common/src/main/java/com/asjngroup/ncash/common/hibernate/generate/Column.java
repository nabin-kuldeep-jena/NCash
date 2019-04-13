package com.asjngroup.ncash.common.hibernate.generate;

import com.asjngroup.ncash.common.database.hibernate.ColumnDataType;

public class Column implements Cloneable {
	private String columnName;
	private int sequenceNumber;
	private ColumnDataType dataType;
	private int length;
	private boolean isMandatory;
	private boolean isPrimaryKey;
	private String displayName;
	private Table table;

	public Column() {
	}

	public Column(String paramString, ColumnDataType paramColumnDataType,
			boolean paramBoolean) {
		this(paramString, paramColumnDataType, 0, paramBoolean);
	}

	public Column(String paramString, ColumnDataType paramColumnDataType,
			boolean paramBoolean, Table paramTable) {
		this.columnName = paramString;
		this.dataType = paramColumnDataType;
		this.isMandatory = paramBoolean;
		this.table = paramTable;
	}

	public Column(String paramString, ColumnDataType paramColumnDataType, int paramInt,
			boolean paramBoolean, Table paramTable) {
		this(paramString, paramColumnDataType, paramBoolean, paramTable);
		this.length = paramInt;
	}

	public Column(String paramString, ColumnDataType paramColumnDataType, int paramInt,
			boolean paramBoolean) {
		this.columnName = paramString.toLowerCase();
		this.dataType = paramColumnDataType;
		this.length = paramInt;
		this.isMandatory = paramBoolean;
	}

	public Column(String paramString1, String paramString2,
			ColumnDataType paramColumnDataType, int paramInt, boolean paramBoolean) {
		this(paramString1, paramColumnDataType, paramInt, paramBoolean);
		this.displayName = paramString2;
	}

	public Column(String paramString, ColumnDataType paramColumnDataType,
			boolean paramBoolean1, int paramInt, boolean paramBoolean2,
			Table paramTable) {
		this(paramString, paramColumnDataType, paramColumnDataType.getLength(),
				paramBoolean1);
		this.sequenceNumber = paramInt;
		this.isPrimaryKey = paramBoolean2;
		this.table = paramTable;
	}

	public Column copy() {
		Column localColumn = new Column(this.columnName, this.dataType,
				this.length, this.isMandatory);
		localColumn.sequenceNumber = this.sequenceNumber;
		localColumn.isPrimaryKey = this.isPrimaryKey;
		localColumn.table = null;
		return localColumn;
	}

	public boolean equals(Object paramObject) {
		if (paramObject == null)
			return false;
		if (!(paramObject instanceof Column))
			return false;
		Column localColumn = (Column) paramObject;
		if (!(this.columnName.equals(localColumn.columnName)))
			return false;
		if ((!(this.dataType.equals(localColumn.dataType)))
				&& ((((this.dataType != ColumnDataType.Decimal) && (this.dataType != ColumnDataType.Long)) || ((localColumn.dataType != ColumnDataType.Decimal) && (localColumn.dataType != ColumnDataType.Long)))))
			return false;
		if ((this.dataType == ColumnDataType.String)
				&& (this.length != localColumn.length))
			return false;
		return (this.isMandatory == localColumn.isMandatory);
	}

	public String getColumnName() {
		return this.columnName;
	}

	public ColumnDataType getDataType() {
		return this.dataType;
	}

	public int getLength() {
		return this.length;
	}

	public boolean isMandatory() {
		return this.isMandatory;
	}

	public void setColumnName(String paramString) {
		this.columnName = paramString.toLowerCase();
	}

	public void setDataType(ColumnDataType paramColumnDataType) {
		this.dataType = paramColumnDataType;
	}

	public void setLength(int paramInt) {
		this.length = paramInt;
	}

	public void setMandatory(boolean paramBoolean) {
		this.isMandatory = paramBoolean;
	}

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(int paramInt) {
		this.sequenceNumber = paramInt;
	}

	public boolean isPrimaryKey() {
		return this.isPrimaryKey;
	}

	public void setPrimaryKey(boolean paramBoolean) {
		this.isPrimaryKey = paramBoolean;
	}

	public Table getTable() {
		return this.table;
	}

	public void setTable(Table paramTable) {
		this.table = paramTable;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String paramString) {
		this.displayName = paramString;
	}

	public Column clone() throws CloneNotSupportedException {
		Column localColumn = (Column) super.clone();
		return localColumn;
	}

	public boolean isAuditEnabled() {
		return false;
	}
}