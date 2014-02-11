package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.impl.CriteriaFiltrationWorker;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class FilterByCriteriaBuilder<DataType> implements WorkerBuilder<FiltrationWorker<DataType>> {

    private FilterCriteria<DataType> criteria;

    public FilterByCriteriaBuilder(FilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    public FiltrationWorker<DataType> build() {
        return new CriteriaFiltrationWorker<DataType>(criteria);
    }

}
