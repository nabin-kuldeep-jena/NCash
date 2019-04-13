package com.asjngroup.ncash.common.hibernate.generate;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.util.StringHelper;

public class Index implements Cloneable {
	private String indexName;
	private boolean isUnique;
	private boolean isClustered;
	private boolean isBusinessConstraint;
	private boolean isDisplayName;
	private boolean isIgnoreDupKey;
	private String columnList;
	private Table table;

	public Index() {
	}

	public Index(String paramString, boolean paramBoolean1,
			boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4) {
		this.indexName = paramString.toLowerCase();
		this.isUnique = paramBoolean1;
		this.isClustered = paramBoolean2;
		this.isBusinessConstraint = paramBoolean3;
		this.isDisplayName = paramBoolean4;
	}

	public Index(String paramString, boolean paramBoolean1,
			boolean paramBoolean2, boolean paramBoolean3,
			boolean paramBoolean4, boolean paramBoolean5, Table paramTable) {
		this.indexName = paramString.toLowerCase();
		this.isUnique = paramBoolean1;
		this.isClustered = paramBoolean2;
		this.isBusinessConstraint = paramBoolean3;
		this.isIgnoreDupKey = paramBoolean5;
		this.isDisplayName = paramBoolean4;
		this.table = paramTable;
	}

	public Index copy() {
		Index localIndex = new Index(this.indexName, this.isUnique,
				this.isClustered, this.isBusinessConstraint, this.isDisplayName);
		Iterator localIterator = getColumnList().iterator();
		while (localIterator.hasNext()) {
			String str = (String) localIterator.next();
			localIndex.addColumn(str);
		}
		localIndex.table = null;
		return localIndex;
	}

	public void addColumn(String paramString) {
		if ((this.columnList == null) || (this.columnList.length() == 0)) {
			this.columnList = paramString.toLowerCase();
		} else {
			Index tmp36_35 = this;
			tmp36_35.columnList = tmp36_35.columnList + ","
					+ paramString.toLowerCase();
		}
	}

	public boolean equals(Object paramObject) {
		if (paramObject == null)
			return false;
		if (!(paramObject instanceof Index))
			return false;
		Index localIndex = (Index) paramObject;
		if (!(this.indexName.equals(localIndex.indexName)))
			return false;
		if (this.isUnique != localIndex.isUnique)
			return false;
		if (this.isClustered != localIndex.isClustered)
			return false;
		if (this.isIgnoreDupKey != localIndex.isIgnoreDupKey)
			return false;
		return (this.columnList.equals(localIndex.columnList));
	}

	public void setColumnList(String paramString) {
		this.columnList = paramString.toLowerCase();
	}

	public List<String> getColumnList() {
		return Arrays.asList(StringHelper.split(this.columnList, ","));
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String paramString) {
		this.indexName = paramString.toLowerCase();
	}

	public boolean isUnique() {
		return this.isUnique;
	}

	public void setUnique(boolean paramBoolean) {
		this.isUnique = paramBoolean;
	}

	public boolean isClustered() {
		return this.isClustered;
	}

	public void setClustered(boolean paramBoolean) {
		this.isClustered = paramBoolean;
	}

	public boolean isBusinessConstraint() {
		return this.isBusinessConstraint;
	}

	public void setBusinessConstraint(boolean paramBoolean) {
		this.isBusinessConstraint = paramBoolean;
	}

	public boolean isDisplayName() {
		return this.isDisplayName;
	}

	public void setDisplayName(boolean paramBoolean) {
		this.isDisplayName = paramBoolean;
	}

	public void setIgnoreDupKey(boolean paramBoolean) {
		this.isIgnoreDupKey = paramBoolean;
	}

	public boolean isIgnoreDupKey() {
		return this.isIgnoreDupKey;
	}

	public void setTable(Table paramTable) {
		this.table = paramTable;
	}

	public Table getTable() {
		return this.table;
	}

	public Index clone() throws CloneNotSupportedException {
		Index localIndex = (Index) super.clone();
		return localIndex;
	}
}