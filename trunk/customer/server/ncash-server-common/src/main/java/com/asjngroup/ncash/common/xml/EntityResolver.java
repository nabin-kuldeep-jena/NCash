package com.asjngroup.ncash.common.xml;

public interface EntityResolver
{
    public void postUpdateEntityObject( XMLTreeBuilder.XMLTreeState stack, Object entityObject ) throws XMLTreeException;
}
