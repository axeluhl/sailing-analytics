package com.sap.sse.datamining.impl.criterias;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.datamining.components.FilterCriterion;

public abstract class CompoundFilterCriterion<DataType> implements FilterCriterion<DataType> {
    
    private Collection<FilterCriterion<DataType>> criterias;

    public CompoundFilterCriterion() {
        this.criterias = Collections.newSetFromMap(new ConcurrentHashMap<FilterCriterion<DataType>, Boolean>());
    }
    
    public void addCriteria(FilterCriterion<DataType> criteria) {
        criterias.add(criteria);
    }
    
    protected Collection<FilterCriterion<DataType>> getCriterias() {
        return criterias;
    }

}
