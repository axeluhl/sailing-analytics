package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.criterias.NonFilteringFilterCriterion;

public abstract class AbstractDataRetrieverChainBuilder<DataSourceType> implements DataRetrieverChainBuilder<DataSourceType> {
    
    private final List<Class<?>> retrievableDataTypes;

    private final TypeSafeFilterCriterionCollection filters;
    private final TypeSafeResultReceiverCollection receivers;
    private int currentRetrievedDataTypeIndex;

    /**
     * @param retrievableDataTypes The retrievable data types, sorted from general to specific types.<br>
     *                             This means, that the most generic data type (e.g. Regatta) is first and the most
     *                             specific data type (e.g. GPSFix) is last.
     */
    public AbstractDataRetrieverChainBuilder(List<Class<?>> retrievableDataTypes) {
        this.retrievableDataTypes = new ArrayList<>(retrievableDataTypes);
        
        filters = new TypeSafeFilterCriterionCollection();
        receivers = new TypeSafeResultReceiverCollection();
        currentRetrievedDataTypeIndex = 0;
    }

    @Override
    public Class<?> getCurrentRetrievedDataType() {
        return retrievableDataTypes.get(currentRetrievedDataTypeIndex);
    }

    @SuppressWarnings("unchecked") // Checking type safety with comparison of the classes
    @Override
    public <T> DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<T> filter) {
        Class<?> currentRetrievedDataType = getCurrentRetrievedDataType();
        if (!filter.getElementType().isAssignableFrom(currentRetrievedDataType)) {
            throw new IllegalArgumentException("The given filter (with element type '" + filter.getElementType().getSimpleName()
                                               + "') isn't able to match the current retrieved data type '" + currentRetrievedDataType.getSimpleName() + "'");
        }

        filters.setCriterion((Class<T>) currentRetrievedDataType, filter);
        return this;
    }

    @SuppressWarnings("unchecked") // Checking type safety with comparison of the classes
    @Override
    public <T> DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<T> resultReceiver) {
        Class<?> currentRetrievedDataType = getCurrentRetrievedDataType();
        if (!resultReceiver.getInputType().isAssignableFrom(currentRetrievedDataType)) {
            throw new IllegalArgumentException("The given result receiver (with input type '" + resultReceiver.getInputType().getSimpleName()
                    + "') isn't able to process the current retrieved data type '" + currentRetrievedDataType.getSimpleName() + "'");
        }
        
        receivers.addResultReceiver((Class<T>) getCurrentRetrievedDataType(), resultReceiver);
        return this;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> stepDeeper() {
        currentRetrievedDataTypeIndex++;
        return this;
    }

    @Override
    public Processor<DataSourceType> build() {
        Processor<?> previousRetriever = null;
        for (int retrievedDataTypeIndex = currentRetrievedDataTypeIndex; retrievedDataTypeIndex >= 0; retrievedDataTypeIndex--) {
            Class<?> retrievedDataType = retrievableDataTypes.get(retrievedDataTypeIndex);
            FilterCriterion<?> filter = filters.getCriterion(retrievedDataType);
            filter = filter != null ? filter : new NonFilteringFilterCriterion<>(retrievedDataType);
            
            Collection<?> storedResultReceivers = receivers.getResultReceivers(retrievedDataType);
            Collection<Processor<?>> resultReceivers = storedResultReceivers != null ? new ArrayList<Processor<?>>((Collection<Processor<?>>) storedResultReceivers) : new ArrayList<Processor<?>>();
            if (previousRetriever != null) {
                resultReceivers.add(previousRetriever);
            }
            
            previousRetriever = createRetrieverFor(retrievedDataType, filter, resultReceivers);
        }
        
        return (Processor<DataSourceType>) previousRetriever;
    }

    protected abstract Processor<?> createRetrieverFor(Class<?> retrievedDataType, FilterCriterion<?> filter,
            Collection<Processor<?>> resultReceivers);

}
