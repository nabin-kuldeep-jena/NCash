
package com.asjngroup.ncash.framework.generic.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EntityRef")
public class EntityRef {

    private String entityName;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

}
