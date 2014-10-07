package com.sap.sse.datamining;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public interface DataRetrieverChainBuilder<DataSourceType> {

    public Class<?> getCurrentRetrievedDataType();

    public DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<?> filter);
    public DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<?, ?> resultReceiver);

    public boolean canStepDeeper();
    public DataRetrieverChainBuilder<DataSourceType> stepDeeper();

    public Processor<DataSourceType, ?> build();

}
