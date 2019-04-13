
package com.asjngroup.ncash.framework.generic.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EntityLink")
public class EntityLink {

    private String rel;

    private String method = "GET";

    private String href;

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
