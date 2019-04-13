package com.asjngroup.ncash.common.database.helper;

public class MysqlIdGenerator extends AbstractIdGenerator
{
	protected String getSelectQuery( String objectName, long required )
	{
		return "select non_current_no from next_object_no where non_object_name = '" + objectName + "'";
	}

	protected String getUpdateQuery( String objectName, long required )
	{
		return "update next_object_no set non_current_no = non_current_no + " + required + " where non_object_name = '" + objectName + "'";
	}

	protected String getInsertQuery( String objectName, long required )
	{
		return "insert into next_object_no ( non_current_no, non_object_name ) values ( " + required + ", '" + objectName + "' )";
	}
}
