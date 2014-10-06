package com.sap.sse.datamining;

import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;

public interface DataRetrieverChainDefinition<DataSourceType> {
    
    public Class<DataSourceType> getDataSourceType();

    public <ResultType> void startWith(Class<Processor<DataSourceType, ResultType>> retrieverType, Class<ResultType> retrievedDataType);

    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void
           addAsLast(Class<Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
                     Class<Processor<NextInputType, NextResultType>> nextRetrieverType,
                     Class<NextResultType> retrievedDataType);
    
    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor);

}
