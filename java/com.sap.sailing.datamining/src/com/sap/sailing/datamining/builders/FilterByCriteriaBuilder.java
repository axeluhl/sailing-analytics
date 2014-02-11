package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.FiltrationWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.CriteriaFiltrationWorker;

public class FilterByCriteriaBuilder<DataType> implements WorkerBuilder<FiltrationWorker<DataType>> {

    private ConcurrentFilterCriteria<DataType> criteria;

    public FilterByCriteriaBuilder(ConcurrentFilterCriteria<DataType> criteria) {
        this.criteria = criteria;
    }

    @Override
    public FiltrationWorker<DataType> build() {
        return new CriteriaFiltrationWorker<DataType>(criteria);
    }

}
