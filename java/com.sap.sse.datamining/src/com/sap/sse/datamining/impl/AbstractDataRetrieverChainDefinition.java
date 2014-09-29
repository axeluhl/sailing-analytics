package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractDataRetrieverChainDefinition<DataSourceType> implements DataRetrieverChainDefinition<DataSourceType> {

    private final Class<? super DataSourceType> dataSourceType;
    private final List<Class<?>> retrievableDataTypes;

    /**
     * @param dataSourceType The input type for the first processor in the retriever chain.
     * @param retrievableDataTypes The retrievable data types, sorted from general to specific types.<br>
     *                             This means, that the most generic data type (e.g. Regatta) is first and the most
     *                             specific data type (e.g. GPSFix) is last.
     */
    public AbstractDataRetrieverChainDefinition(Class<? super DataSourceType> dataSourceType, List<Class<?>> retrievableDataTypes) {
        this.dataSourceType = dataSourceType;
        this.retrievableDataTypes = new ArrayList<>(retrievableDataTypes);
    }

    @Override
    public Class<? super DataSourceType> getDataSourceType() {
        return dataSourceType;
    }

    @Override
    public boolean canRetrieve(Class<?> dataType) {
        return retrievableDataTypes.contains(dataType);
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> startBuilding() {
        return new AbstractDataRetrieverChainBuilder<DataSourceType>(retrievableDataTypes) {
            @Override
            protected Processor<?> createRetrieverFor(Class<?> retrievedDataType, FilterCriterion<?> filter,
                    Collection<Processor<?>> resultReceivers) {
                return AbstractDataRetrieverChainDefinition.this.createRetrieverFor(retrievedDataType, filter, resultReceivers);
            }
        };
    }

    protected abstract Processor<?> createRetrieverFor(Class<?> retrievedDataType, FilterCriterion<?> filter,
            Collection<Processor<?>> resultReceivers);

}
