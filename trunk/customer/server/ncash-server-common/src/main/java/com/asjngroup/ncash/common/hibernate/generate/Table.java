package com.asjngroup.ncash.common.hibernate.generate;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.ColumnDataType;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;

public class Table implements Cloneable {
	private String tableName;
	private String tablePrefix;
	private List<Column> columns;
	private List<Index> indexes;
	private DataBase database;

	public Table() {
		this.columns = new ArrayList();
		this.indexes = new ArrayList();
	}

	public Table(String paramString) {
		this(paramString, null);
	}

	public Table(String paramString1, String paramString2) {
		this.columns = new ArrayList();
		this.indexes = new ArrayList();
		this.tableName = paramString1.toLowerCase();
		if (paramString2 == null)
			return;
		this.tablePrefix = paramString2.toLowerCase();
	}

	public Table copy() {
		Table localTable = new Table(this.tableName, this.tablePrefix);
		localTable.database = this.database;
		Iterator localIterator = this.columns.iterator();
		Object localObject;
		while (localIterator.hasNext()) {
			localObject = (Column) localIterator.next();
			localTable.getColumns().add(((Column) localObject).copy());
		}
		localIterator = this.indexes.iterator();
		while (localIterator.hasNext()) {
			localObject = (Index) localIterator.next();
			localTable.getIndexes().add(((Index) localObject).copy());
		}
		return ((Table) localTable);
	}

	public Column findColumn(String paramString) {
		Iterator localIterator = this.columns.iterator();
		while (localIterator.hasNext()) {
			Column localColumn = (Column) localIterator.next();
			if (localColumn.getColumnName().equals(paramString))
				return localColumn;
		}
		return null;
	}

	public Index findIndex(String paramString) {
		Iterator localIterator = this.indexes.iterator();
		while (localIterator.hasNext()) {
			Index localIndex = (Index) localIterator.next();
			if (localIndex.getIndexName().equals(paramString))
				return localIndex;
		}
		return null;
	}

	public Index getClusteredIndex() {
		Iterator localIterator = this.indexes.iterator();
		while (localIterator.hasNext()) {
			Index localIndex = (Index) localIterator.next();
			if (localIndex.isClustered())
				return localIndex;
		}
		return null;
	}

	public List<String> getColumnNames() {
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = this.columns.iterator();
		while (localIterator.hasNext()) {
			Column localColumn = (Column) localIterator.next();
			localArrayList.add(localColumn.getColumnName());
		}
		return localArrayList;
	}

	public ColumnDataType[] getColumnTypes() {
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = this.columns.iterator();
		while (localIterator.hasNext()) {
			Column localColumn = (Column) localIterator.next();
			localArrayList.add(localColumn.getDataType());
		}
		return ((ColumnDataType[]) localArrayList.toArray(new ColumnDataType[0]));
	}

	public boolean equals(Object paramObject) {
		if (!(isTableStructureEqual(paramObject)))
			return false;
		return (areIndexesEqual(paramObject));
	}

	public boolean isTableStructureEqual(Object paramObject) {
		if (paramObject == null)
			return false;
		if (!(paramObject instanceof Table))
			return false;
		Table localTable = (Table) paramObject;
		if (!(localTable.tableName.equals(this.tableName)))
			return false;
		if (this.columns.size() != localTable.columns.size())
			return false;
		Column localColumn1;
		Column localColumn2;
		for (int i = 0; i < this.columns.size(); ++i) {
			localColumn1 = (Column) this.columns.get(i);
			localColumn2 = (Column) localTable.columns.get(i);
			if (!(localColumn1.equals(localColumn2)))
				return false;
		}
		Iterator localIterator = localTable.columns.iterator();
		while (localIterator.hasNext()) {
			localColumn1 = (Column) localIterator.next();
			localColumn2 = findColumn(localColumn1.getColumnName());
			if (localColumn2 == null)
				return false;
		}
		return true;
	}

	public boolean areIndexesEqual(Object paramObject) {
		if (paramObject == null)
			return false;
		if (!(paramObject instanceof Table))
			return false;
		Table localTable = (Table) paramObject;
		if (!(localTable.tableName.equals(this.tableName)))
			return false;
		Iterator localIterator = this.indexes.iterator();
		Index localIndex1;
		Index localIndex2;
		while (localIterator.hasNext()) {
			localIndex1 = (Index) localIterator.next();
			localIndex2 = localTable.findIndex(localIndex1.getIndexName());
			if (localIndex2 == null)
				return false;
			if (!(localIndex1.equals(localIndex2)))
				return false;
		}
		localIterator = localTable.indexes.iterator();
		while (localIterator.hasNext()) {
			localIndex1 = (Index) localIterator.next();
			localIndex2 = findIndex(localIndex1.getIndexName());
			if (localIndex2 == null)
				return false;
		}
		return true;
	}

	public Column getPrimaryKeyColumn() {
		String str1 = getTablePrefix();
		if (str1 == null)
			throw new NCashRuntimeException(
					"Table prefix for %1 is null. Unable to determine the primary key column.",
					new Object[] { getTableName() });
		String str2 = SchemaHelper.entityNameToDatabaseName(SchemaHelper
				.getIdPropertyName(getTablePrefix()));
		Iterator localIterator = this.columns.iterator();
		while (localIterator.hasNext()) {
			Column localColumn = (Column) localIterator.next();
			if (localColumn.getColumnName().equals(str2))
				return localColumn;
		}
		return null;
	}

	public void setFieldAuditing(boolean paramBoolean) {
		if (paramBoolean) {
			if (findColumn("modified_dttm") != null)
				return;
			this.columns.add(new Column("modified_dttm", ColumnDataType.DateTime,
					true));
			this.columns.add(new Column("created_dttm", ColumnDataType.DateTime,
					true));
			this.columns
					.add(new Column("modified_usr_id", ColumnDataType.Int, true));
			this.columns.add(new Column("created_usr_id", ColumnDataType.Int, true));
		} else {
			Column localColumn = findColumn("modified_dttm");
			if (localColumn == null)
				return;
			this.columns.remove(localColumn);
			this.columns.remove(findColumn("created_dttm"));
			this.columns.remove(findColumn("modified_usr_id"));
			this.columns.remove(findColumn("created_usr_id"));
		}
	}

	public String getTableName() {
		return this.tableName;
	}

	public String getTablePrefix() {
		return this.tablePrefix;
	}

	public List<Column> getColumns() {
		return this.columns;
	}

	public List<Index> getIndexes() {
		return this.indexes;
	}

	public void setTableName(String paramString) {
		this.tableName = paramString.toLowerCase();
	}

	public void setTablePrefix(String paramString) {
		this.tablePrefix = paramString.toLowerCase();
	}

	public void setColumns(List<Column> paramList) {
		this.columns = paramList;
	}

	public void setIndexes(List<Index> paramList) {
		this.indexes = paramList;
	}

	public void setDatabase(DataBase paramDataBase) {
		this.database = paramDataBase;
	}

	public DataBase getDatabase() {
		return this.database;
	}

	public Table clone() throws CloneNotSupportedException {
		Table localTable = (Table) super.clone();
		localTable.setColumns(new ArrayList());
		Iterator localIterator = getColumns().iterator();
		Object localObject1;
		Object localObject2;
		while (localIterator.hasNext()) {
			localObject1 = (Column) localIterator.next();
			localObject2 = ((Column) localObject1).clone();
			((Column) localObject2).setTable(localTable);
			localTable.getColumns().add(( Column ) localObject2);
		}
		localTable.setIndexes(new ArrayList());
		localIterator = getIndexes().iterator();
		while (localIterator.hasNext()) {
			localObject1 = (Index) localIterator.next();
			localObject2 = ((Index) localObject1).clone();
			((Index) localObject2).setTable(localTable);
			localTable.getIndexes().add(( Index ) localObject2);
		}
		return ((Table) (Table) localTable);
	}

	public boolean isIndexColumn(Column paramColumn) {
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = getIndexes().iterator();
		while (localIterator.hasNext()) {
			Index localIndex = (Index) localIterator.next();
			localArrayList.add(localIndex);
		}
		return localArrayList.contains(paramColumn.getColumnName());
	}
}