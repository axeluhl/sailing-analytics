package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public abstract class CompoundFilterCriteria<DataType> implements ConcurrentFilterCriteria<DataType> {
    
    private Collection<ConcurrentFilterCriteria<DataType>> criterias;

    public CompoundFilterCriteria() {
        this.criterias = Collections.newSetFromMap(new ConcurrentHashMap<ConcurrentFilterCriteria<DataType>, Boolean>());
    }
    
    public void addCriteria(ConcurrentFilterCriteria<DataType> criteria) {
        criterias.add(criteria);
    }
    
    protected Collection<ConcurrentFilterCriteria<DataType>> getCriterias() {
        return criterias;
    }

}
