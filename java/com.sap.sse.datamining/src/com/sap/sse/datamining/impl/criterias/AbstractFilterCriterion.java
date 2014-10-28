package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public abstract class AbstractFilterCriterion<ElementType> implements FilterCriterion<ElementType> {
    
    private final Class<ElementType> elementType;

    public AbstractFilterCriterion(Class<ElementType> elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public Class<ElementType> getElementType() {
        return elementType;
    }
    
}
