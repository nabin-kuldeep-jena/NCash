
package com.asjngroup.ncash.common.hibernate.generate;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataBase implements Cloneable {
	private String DatabaseName;
	public boolean IsObjectMapped;
	private List<Table> tables = new ArrayList();

	public DataBase() {
	}

	public DataBase(String paramString) {
		this.DatabaseName = paramString;
	}

	public DataBase(String paramString, List<Table> paramList) {
		this.DatabaseName = paramString;
		this.tables = paramList;
	}

	public Table findTable(String paramString) {
		Iterator localIterator = getTables().iterator();
		while (localIterator.hasNext()) {
			Table localTable = (Table) localIterator.next();
			if (localTable.getTableName().equalsIgnoreCase(paramString))
				return localTable;
		}
		return null;
	}

	public Table removeTable(String paramString) {
		Iterator localIterator = getTables().iterator();
		while (localIterator.hasNext()) {
			Table localTable = (Table) localIterator.next();
			if (localTable.getTableName().equalsIgnoreCase(paramString)) {
				localIterator.remove();
				return localTable;
			}
		}
		return null;
	}

	public List<Table> getTables() {
		return this.tables;
	}

	public String getDatabaseName() {
		return this.DatabaseName;
	}

	public void setDatabaseName(String paramString) {
		this.DatabaseName = paramString;
	}

	public Object clone() throws CloneNotSupportedException {
		DataBase localDataBase = new DataBase();
		Iterator localIterator = getTables().iterator();
		while (localIterator.hasNext()) {
			Table localTable1 = (Table) localIterator.next();
			Table localTable2 = localTable1.clone();
			localDataBase.getTables().add(localTable2);
			localTable2.setDatabase(localDataBase);
		}
		localDataBase.DatabaseName = this.DatabaseName;
		localDataBase.IsObjectMapped = this.IsObjectMapped;
		return localDataBase;
	}
}