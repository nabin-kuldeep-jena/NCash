package com.asjngroup.ncash.framework.generic.util;

public class EntityRelationUtil
{

	public static EntityRelationModel getEntityRealtionByKey( String relationalEntity, String targetEntity )
	{

		switch( relationalEntity )
		{
		case "UserTbl":
			return getUserRealtionWithEntity(relationalEntity, targetEntity );
		default:
			break;
		}
		return null;
	}

	protected static EntityRelationModel getUserRealtionWithEntity( String relationalEntity, String targetEntity )
	{
		switch( targetEntity )
		{
		case "StoreTbl":
			EntityRelationModel buildEntityRelationModel = buildEntityRelationModel(relationalEntity,targetEntity ,"",false );
			buildEntityRelationModel.setDirectQuery( true );
			buildEntityRelationModel.setRelationShip( "select str from "+targetEntity+" usr , "+targetEntity+" str " );
			return buildEntityRelationModel;
		default:
			break;
		}
		return null;
	}

	protected static EntityRelationModel buildEntityRelationModel( String sourceEntity, String targetEntity, String relationShip,boolean isMultipleRelationShip)
	{
		EntityRelationModel entityRelationModel=new EntityRelationModel();
		entityRelationModel.setMultipleRelationShip( isMultipleRelationShip );
		entityRelationModel.setRelationShip( relationShip );
		entityRelationModel.setSourceEntity( sourceEntity );
		entityRelationModel.setTargetEntity( targetEntity );
		return entityRelationModel;
	}
}
