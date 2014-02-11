package com.sap.sailing.datamining.impl;

import com.sap.sse.datamining.components.FilterCriteria;

public class CriteriaFiltrationWorker<DataType> extends AbstractFiltrationWorker<DataType> {
    
    private FilterCriteria<DataType> criteria;

    public CriteriaFiltrationWorker(FilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    protected boolean matches(DataType dataEntry) {
        return criteria.matches(dataEntry);
    }

}
