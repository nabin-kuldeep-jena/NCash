
package com.asjngroup.ncash.framework.generic.services;

import org.hibernate.Session;

import java.util.List;

import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapper;
import com.asjngroup.ncash.framework.generic.models.EntityModel;
import com.asjngroup.ncash.framework.generic.models.EntityRef;


public interface IEntityService
{
	public List<EntityModel> getEntity( String entityName, String entityId, List<Integer> partitions, Session session ,CustomizeDataMapper customizeDataMapper,boolean isLinkReq );

	public List<EntityModel> getEntities( String entityName,String condition, List<Integer> partitions, Integer pageNum, Integer pageSize, Session session ,CustomizeDataMapper customizeDataMapper,boolean isLinkReq);
	
	public List<EntityModel> getEntities( String entityName,String relationEntity, String condition, List<Integer> partitions, Integer pageNum, Integer pageSize, Session session,boolean isLinkReq);

	public EntityRef createEntity( EntityModel entityModel );

	public EntityRef updateEntity( EntityModel entityModel );

	public List<EntityRef> updateEntities( EntityModel entityModel, String condition );

	public List<EntityRef> deleteEntities( String entityName, String condition, List<Integer> partitions );

	public List<EntityRef> deleteEntity( String entityName, String entityId, List<Integer> partitions );

	public List<EntityModel> queryEntities( String entityName, String fields, String aggrFun, String condition, List<Integer> paritions, Integer pageNum, Integer pageSize );
}
