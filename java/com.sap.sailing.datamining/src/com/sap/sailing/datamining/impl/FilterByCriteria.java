package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.FilterCriteria;

public class FilterByCriteria<DataType> implements SingleThreadedFilter<DataType> {
    
    private FilterCriteria<DataType> criteria;

    public FilterByCriteria(FilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    public Collection<DataType> filter(Collection<DataType> data) {
        Collection<DataType> filteredData = new ArrayList<DataType>();
        for (DataType dataEntry : data) {
            if (criteria.matches(dataEntry)) {
                filteredData.add(dataEntry);
            }
        }
        return filteredData;
    }

}
