
package com.asjngroup.ncash.framework.generic.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@XmlRootElement(name = "EntityModel")
public class EntityModel extends EntityRef {

    private Map<String, Object> properties = new LinkedHashMap<>();

    private List<EntityLink> links;

    public void addProperty(String propName, Object propValue) {
        properties.put(propName, propValue);
    }

    public void addLink(String rel, String href) {
        if (links == null) {
            links = new ArrayList<>();
        }
        EntityLink link = new EntityLink();
        link.setHref(href);
        link.setRel(rel);
        // default 
        link.setMethod("GET");

        links.add(link);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<EntityLink> getLinks() {
        return links;
    }

    public void setLinks(List<EntityLink> links) {
        this.links = links;
    }

    public void addLink(EntityLink link) {
        if (links == null) {
            links = new ArrayList<>();
        }
        links.add(link);
    }

    //TODO: CHANGE THIS
    @Override
    public String toString() {
        String result = null;
        try {
            result = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void prependLink(String baseUri) {
        if (links != null) {
            for (EntityLink entityLink : links) {
                entityLink.setHref(baseUri + "/" + entityLink.getHref());
            }
        }
    }

}
