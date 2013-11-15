package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.SingleThreadedFilter;

public class FilterByCriteriaBuilder<DataType> implements WorkerBuilder<SingleThreadedFilter<DataType>> {

    private FilterCriteria<DataType> criteria;

    public FilterByCriteriaBuilder(FilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    public SingleThreadedFilter<DataType> build() {
        return new FilterByCriteria<DataType>(criteria);
    }

}
