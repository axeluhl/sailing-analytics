package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public class FilterByCriteria<DataType> extends AbstractSingleThreadedFilter<DataType> {
    
    private ConcurrentFilterCriteria<DataType> criteria;

    public FilterByCriteria(ConcurrentFilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    protected Collection<DataType> filterData() {
        Collection<DataType> filteredData = new ArrayList<DataType>();
        for (DataType dataEntry : data) {
            if (criteria.matches(dataEntry)) {
                filteredData.add(dataEntry);
            }
        }
        return filteredData;
    }

}
