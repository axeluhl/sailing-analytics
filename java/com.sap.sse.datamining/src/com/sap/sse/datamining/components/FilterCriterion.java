package com.sap.sse.datamining.components;

public interface FilterCriterion<ElementType> {
    
    public boolean matches(ElementType element);
    
    public Class<ElementType> getElementType();

}
