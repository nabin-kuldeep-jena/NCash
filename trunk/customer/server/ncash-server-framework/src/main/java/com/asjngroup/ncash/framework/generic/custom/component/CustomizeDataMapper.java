package com.asjngroup.ncash.framework.generic.custom.component;

import org.hibernate.metadata.ClassMetadata;

import com.asjngroup.ncash.framework.generic.models.EntityModel;

public interface CustomizeDataMapper
{
	public void mapCutomeData( Object obj,ClassMetadata metadata, EntityModel model );
}
