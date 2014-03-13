package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public class CriteriaFiltrationWorker<DataType> extends AbstractFiltrationWorker<DataType> {
    
    private ConcurrentFilterCriteria<DataType> criteria;

    public CriteriaFiltrationWorker(ConcurrentFilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    protected boolean matches(DataType dataEntry) {
        return criteria.matches(dataEntry);
    }

}
