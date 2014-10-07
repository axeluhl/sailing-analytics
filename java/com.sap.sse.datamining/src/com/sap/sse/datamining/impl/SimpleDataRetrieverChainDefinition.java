package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;

public class SimpleDataRetrieverChainDefinition<DataSourceType> implements
        DataRetrieverChainDefinition<DataSourceType> {

    private final Class<DataSourceType> dataSourceType;
    private final List<DataRetrieverTypeWithInformation<?, ?>> dataRetrieverTypesWithInformation;

    public SimpleDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType) {
        this.dataSourceType = dataSourceType;
        dataRetrieverTypesWithInformation = new ArrayList<>();
    }

    @Override
    public Class<DataSourceType> getDataSourceType() {
        return dataSourceType;
    }

    @Override
    public <ResultType> void startWith(Class<Processor<DataSourceType, ResultType>> retrieverType,
                                       Class<ResultType> retrievedDataType) {
        if (isInitialized()) {
            throw new UnsupportedOperationException("This retriever chain definition already has been started with '"
                                                    + dataRetrieverTypesWithInformation.get(0).getRetrieverType().getSimpleName() + "'");
        }
        checkThatRetrieverHasUsableConstructor(retrieverType);
        
        DataRetrieverTypeWithInformation<?, ?> retrieverTypeWithInformation = new DataRetrieverTypeWithInformation<>(retrieverType, retrievedDataType);
        dataRetrieverTypesWithInformation.add(retrieverTypeWithInformation);
    }

    private boolean isInitialized() {
        return !dataRetrieverTypesWithInformation.isEmpty();
    }

    @Override
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void addAsLast(
            Class<Processor<PreviousInputType, PreviousResultType>> previousRetrieverType,
            Class<Processor<NextInputType, NextResultType>> nextRetrieverType,
            Class<NextResultType> retrievedDataType) {
        if (!isInitialized()) {
            throw new UnsupportedOperationException("This retriever chain definition hasn't been started yet");
        }
        DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation = dataRetrieverTypesWithInformation.get(dataRetrieverTypesWithInformation.size() - 1);
        @SuppressWarnings("unchecked")
        Class<Processor<?, ?>> lastRetrieverInList = (Class<Processor<?, ?>>)(Class<?>) dataRetrieverTypeWithInformation.getRetrieverType();
        if (!lastRetrieverInList.equals(previousRetrieverType)) {
            throw new IllegalArgumentException("The given previousRetrieverType '" + previousRetrieverType.getSimpleName()
                                               + "' doesn't match the last retriever type in the chain '" 
                                               + lastRetrieverInList.getSimpleName() + "'");
        }
        checkThatRetrieverHasUsableConstructor(nextRetrieverType);

        DataRetrieverTypeWithInformation<?, ?> retrieverTypeWithInformation = new DataRetrieverTypeWithInformation<>(nextRetrieverType, retrievedDataType);
        dataRetrieverTypesWithInformation.add(retrieverTypeWithInformation);
    }

    private <InputType, ResultType> void checkThatRetrieverHasUsableConstructor(
            Class<Processor<InputType, ResultType>> retrieverType) {
        try {
            retrieverType.getConstructor(ExecutorService.class, Collection.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given nextRetrieverType '"
                    + retrieverType.getSimpleName() + "'", e);
        }
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor) {
        return new SimpleDataRetrieverChainBuilder<>(executor, dataRetrieverTypesWithInformation);
    }

}
