package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.SingleThreadedFilter;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.FilterByCriteria;

public class FilterByCriteriaBuilder<DataType> implements WorkerBuilder<SingleThreadedFilter<DataType>> {

    private ConcurrentFilterCriteria<DataType> criteria;

    public FilterByCriteriaBuilder(ConcurrentFilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    public SingleThreadedFilter<DataType> build() {
        return new FilterByCriteria<DataType>(criteria);
    }

}
