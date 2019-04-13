package com.asjngroup.ncash.common.database.helper;

public class MySqlSequenceGenerator extends AbstractSequenceNumberGenerator
{
	protected String getSelectQuery( String objectName, long required )
	{
		return "select sng_current_no from sequence_num_generator where sng_object_name = '" + objectName + "'";
	}

	protected String getUpdateQuery( String objectName, long required )
	{
		return "update sequence_num_generator set sng_current_no = sng_current_no + " + required + " where sng_object_name = '" + objectName + "'";
	}

	protected String getInsertQuery( String objectName, long required )
	{
		return "insert into sequence_num_generator ( sng_current_no, sng_object_name ) values ( " + required + ", '" + objectName + "' )";
	}
}
