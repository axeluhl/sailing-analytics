package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class SimpleDataRetrieverChainDefinition<DataSourceType> implements
        DataRetrieverChainDefinition<DataSourceType> {

    private final UUID id;
    private final String nameMessageKey;
    
    private final Class<DataSourceType> dataSourceType;
    private final List<DataRetrieverTypeWithInformation<?, ?>> dataRetrieverTypesWithInformation;

    public SimpleDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType, String nameMessageKey) {
        id = UUID.randomUUID();
        this.nameMessageKey = nameMessageKey;
        
        this.dataSourceType = dataSourceType;
        dataRetrieverTypesWithInformation = new ArrayList<>();
    }

    public SimpleDataRetrieverChainDefinition(DataRetrieverChainDefinition<DataSourceType> dataRetrieverChainDefinition, String nameMessageKey) {
        this(dataRetrieverChainDefinition.getDataSourceType(), nameMessageKey);
        dataRetrieverTypesWithInformation.addAll(dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation());
    }
    
    @Override
    public UUID getUUID() {
        return id;
    }
    
    @Override
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        return stringMessages.get(locale, nameMessageKey);
    }

    @Override
    public Class<DataSourceType> getDataSourceType() {
        return dataSourceType;
    }
    
    @Override
    public Class<?> getRetrievedDataType() {
        return dataRetrieverTypesWithInformation.get(dataRetrieverTypesWithInformation.size() - 1).getRetrievedDataType();
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
    public List<? extends DataRetrieverTypeWithInformation<?, ?>> getDataRetrieverTypesWithInformation() {
        return dataRetrieverTypesWithInformation;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor) {
        return new SimpleDataRetrieverChainBuilder<>(executor, dataRetrieverTypesWithInformation);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dataRetrieverTypesWithInformation == null) ? 0 : dataRetrieverTypesWithInformation.hashCode());
        result = prime * result + ((dataSourceType == null) ? 0 : dataSourceType.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleDataRetrieverChainDefinition<?> other = (SimpleDataRetrieverChainDefinition<?>) obj;
        if (dataRetrieverTypesWithInformation == null) {
            if (other.dataRetrieverTypesWithInformation != null)
                return false;
        } else if (!dataRetrieverTypesWithInformation.equals(other.dataRetrieverTypesWithInformation))
            return false;
        if (dataSourceType == null) {
            if (other.dataSourceType != null)
                return false;
        } else if (!dataSourceType.equals(other.dataSourceType))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
