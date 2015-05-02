package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;

public class SingleDataRetrieverChainDefinition<DataSourceType, DataType> extends SimpleDataRetrieverChainDefinition<DataSourceType, DataType> {

    public SingleDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType,
            String nameMessageKey) {
        super(dataSourceType, retrievedDataType, nameMessageKey);
    }
    
    @Override
    public <ResultType> void startWith(Class<? extends Processor<DataSourceType, ResultType>> retrieverType,
            Class<ResultType> retrievedDataType, String retrievedDataTypeMessageKey) {
        super.startWith(retrieverType, retrievedDataType, retrievedDataTypeMessageKey);
        isComplete = true;
    }
    
    @Override
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void addAfter(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
            Class<? extends Processor<NextInputType, NextResultType>> nextRetrieverType,
            Class<NextResultType> retrievedDataType, String retrievedDataTypeMessageKey) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType> void endWith(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
            Class<? extends Processor<NextInputType, DataType>> lastRetrieverType, Class<DataType> retrievedDataType,
            String retrievedDataTypeMessageKey) {
        throw new UnsupportedOperationException();
    }

}
