package com.asjngroup.ncash.framework.generic.util;

public class EntityRelationModel
{
	private boolean isMultipleRelationShip;
	
	private boolean isDirectQuery;
	
	private String relationShip;
	
	private String sourceEntity;
	
	private String targetEntity;

	

	public boolean isDirectQuery()
	{
		return isDirectQuery;
	}

	public void setDirectQuery( boolean isDirectQuery )
	{
		this.isDirectQuery = isDirectQuery;
	}

	public String getSourceEntity()
	{
		return sourceEntity;
	}

	public void setSourceEntity( String sourceEntity )
	{
		this.sourceEntity = sourceEntity;
	}

	public String getTargetEntity()
	{
		return targetEntity;
	}

	public void setTargetEntity( String targetEntity )
	{
		this.targetEntity = targetEntity;
	}

	public boolean isMultipleRelationShip()
	{
		return isMultipleRelationShip;
	}

	public void setMultipleRelationShip( boolean isMultipleRelationShip )
	{
		this.isMultipleRelationShip = isMultipleRelationShip;
	}

	public String getRelationShip()
	{
		return relationShip;
	}

	public void setRelationShip( String relationShip )
	{
		this.relationShip = relationShip;
	}
}
 