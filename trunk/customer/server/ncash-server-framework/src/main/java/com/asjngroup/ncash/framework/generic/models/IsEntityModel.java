package com.asjngroup.ncash.framework.generic.models;

public interface IsEntityModel
{
	int getId();

	void setId( int id );

	<T> T get( String property );

	<T> void set( String property, T value );
	
	void setPartitionId( Integer partitionId );

	Integer getPartitionId();

	void setVersionId( int versionId );

	int getVersionId();

	void setDeleteFl( Boolean deleteFl );

	Boolean getDeleteFl();

	void setSystemGeneratedFl( Boolean systemGeneratedFl );

	Boolean getSystemGeneratedFl();

	void setEntityCopy( Boolean flag );

	boolean isNew();

	void setIsNew( boolean isNew );

	void setDisplayString( String displayString );

	String getDisplayString();
}