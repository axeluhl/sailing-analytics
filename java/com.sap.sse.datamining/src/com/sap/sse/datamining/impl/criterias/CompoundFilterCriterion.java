package com.sap.sse.datamining.impl.criterias;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.datamining.components.FilterCriterion;

public abstract class CompoundFilterCriterion<ElementType> extends AbstractFilterCriterion<ElementType> {
    
    private Collection<FilterCriterion<ElementType>> criterias;

    public CompoundFilterCriterion(Class<ElementType> elementType) {
        super(elementType);
        this.criterias = Collections.newSetFromMap(new ConcurrentHashMap<FilterCriterion<ElementType>, Boolean>());
    }
    
    public void addCriteria(FilterCriterion<ElementType> criteria) {
        criterias.add(criteria);
    }
    
    protected Collection<FilterCriterion<ElementType>> getCriterias() {
        return criterias;
    }

}
