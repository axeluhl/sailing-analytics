package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.datamining.components.FilterCriteria;

public abstract class CompoundFilterCriteria<DataType> implements FilterCriteria<DataType> {
    
    private Collection<FilterCriteria<DataType>> criterias;

    public CompoundFilterCriteria() {
        this.criterias = Collections.newSetFromMap(new ConcurrentHashMap<FilterCriteria<DataType>, Boolean>());
    }
    
    public void addCriteria(FilterCriteria<DataType> criteria) {
        criterias.add(criteria);
    }
    
    protected Collection<FilterCriteria<DataType>> getCriterias() {
        return criterias;
    }

}
