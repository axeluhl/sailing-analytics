package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.FilterCriteria;

public abstract class CompoundFilterCriteria<DataType> implements FilterCriteria<DataType> {
    
    private Collection<FilterCriteria<DataType>> criterias;

    public CompoundFilterCriteria() {
        this.criterias = new HashSet<FilterCriteria<DataType>>(criterias);
    }
    
    public void addCriteria(FilterCriteria<DataType> criteria) {
        criterias.add(criteria);
    }
    
    protected Collection<FilterCriteria<DataType>> getCriterias() {
        return criterias;
    }

}
