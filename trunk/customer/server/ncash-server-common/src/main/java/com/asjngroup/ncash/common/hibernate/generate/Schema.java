package com.asjngroup.ncash.common.hibernate.generate;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Schema implements Cloneable {
	private List<DataBase> Databases = new ArrayList();

	public DataBase findDatabase(String paramString) {
		Iterator localIterator = this.Databases.iterator();
		while (localIterator.hasNext()) {
			DataBase localDataBase = (DataBase) localIterator.next();
			if (localDataBase.getDatabaseName().equals(paramString))
				return localDataBase;
		}
		return null;
	}

	public List<DataBase> getDatabases() {
		return this.Databases;
	}

	public void setDatabases(List<DataBase> paramList) {
		this.Databases = paramList;
	}

	public Object clone() throws CloneNotSupportedException {
		Schema localSchema = new Schema();
		Iterator localIterator = getDatabases().iterator();
		while (localIterator.hasNext()) {
			DataBase localDataBase = (DataBase) localIterator.next();
			localSchema.getDatabases().add((DataBase) localDataBase.clone());
		}
		return localSchema;
	}
}