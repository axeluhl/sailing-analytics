package com.sap.sse.datamining.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;

public class SimpleDataRetrieverChainBuilder<DataSourceType> implements DataRetrieverChainBuilder<DataSourceType> {
    
    private final ExecutorService executor;
    private final List<DataRetrieverTypeWithInformation<?, ?>> dataRetrieverTypesWithInformation;

    private final Map<Integer, FilterCriterion<?>> filters;
    private final Map<Integer, Collection<Processor<?, ?>>> receivers;
    private int currentRetrieverTypeIndex;

    /**
     * Creates a data retriever chain builder for the given list of {@link DataRetrieverTypeWithInformation}.</br>
     * The list has to match the following conditions to build a valid data retriever chain:
     * <ul>
     *  <li>The first data retriever type has the <code>DataSourceType</code> as <code>InputType</code>.</li>
     *  <li>The order of the data retriever types has to be valid.</br>
     *      This means, that the <code>ResultType</code> of a previous data retriever type has to match
     *      the <code>InputType</code> of the next data retriever type.</li>
     * </ul>
     * <b>This builder won't check these conditions!</b></br>
     * It could be possible, that the construction of a data retriever chain, that doesn't match the conditions
     * work, but the resulting data retriever chain will fail, if you try to retrieve data.</br>
     * If you're not forced to create your own instance of this builder, use {@link DataRetrieverChainDefinition#startBuilding()},
     * that always creates a valid builder.
     * @param dataRetrieverTypesWithInformation
     */
    SimpleDataRetrieverChainBuilder(ExecutorService executor,
            List<DataRetrieverTypeWithInformation<?, ?>> dataRetrieverTypesWithInformation) {
        this.executor = executor;
        this.dataRetrieverTypesWithInformation = new ArrayList<>(dataRetrieverTypesWithInformation);
        
        filters = new HashMap<>();
        receivers = new HashMap<>();
        currentRetrieverTypeIndex = -1;
    }
    
    @Override
    public boolean canStepFurther() {
        return currentRetrieverTypeIndex + 1 < dataRetrieverTypesWithInformation.size();
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> stepFurther() {
        currentRetrieverTypeIndex++;
        return this;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<?> filter) {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException();
        }
        
        if (!filter.getElementType().isAssignableFrom(getCurrentRetrievedDataType())) {
            throw new IllegalArgumentException("The given filter (with element type '" + filter.getElementType().getSimpleName()
                                               + "') isn't able to match the current retrieved data type '" + getCurrentRetrievedDataType().getSimpleName() + "'");
        }

        filters.put(currentRetrieverTypeIndex, filter);
        return this;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<?, ?> resultReceiver) {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException();
        }
        
        if (!resultReceiver.getInputType().isAssignableFrom(getCurrentRetrievedDataType())) {
            throw new IllegalArgumentException("The given result receiver (with input type '" + resultReceiver.getInputType().getSimpleName()
                    + "') isn't able to process the current retrieved data type '" + getCurrentRetrievedDataType().getSimpleName() + "'");
        }

        if (!receivers.containsKey(currentRetrieverTypeIndex)) {
            receivers.put(currentRetrieverTypeIndex, new HashSet<Processor<?, ?>>());
        }
        receivers.get(currentRetrieverTypeIndex).add(resultReceiver);
        return this;
    }

    @Override
    public Class<?> getCurrentRetrievedDataType() {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException();
        }
        
        return dataRetrieverTypesWithInformation.get(currentRetrieverTypeIndex).getRetrievedDataType();
    }
    
    @Override
    public int getCurrentRetrieverLevel() {
        return currentRetrieverTypeIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Processor<DataSourceType, ?> build() {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException();
        }
        
        Processor<?, ?> firstRetriever = null;
        for (int retrieverTypeIndex = currentRetrieverTypeIndex; retrieverTypeIndex >= 0; retrieverTypeIndex--) {
            DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation = dataRetrieverTypesWithInformation.get(retrieverTypeIndex);
            firstRetriever = createRetriever(dataRetrieverTypeWithInformation, firstRetriever, retrieverTypeIndex);
        }
        
        return (Processor<DataSourceType, ?>) firstRetriever;
    }

    private boolean hasBeenInitialized() {
        return currentRetrieverTypeIndex >= 0;
    }
    
    @SuppressWarnings("unchecked")
    private <ResultType> Processor<?, ResultType> createRetriever(DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation, Processor<?, ?> previousRetriever, int retrieverTypeIndex) {
        Class<ResultType> retrievedDataType = (Class<ResultType>) dataRetrieverTypeWithInformation.getRetrievedDataType();
        
        Collection<?> storedResultReceivers = receivers.get(retrieverTypeIndex);
        Collection<Processor<ResultType, ?>> resultReceivers = storedResultReceivers != null ? new ArrayList<Processor<ResultType, ?>>((Collection<Processor<ResultType, ?>>) storedResultReceivers) : new ArrayList<Processor<ResultType, ?>>();
        if (previousRetriever != null) {
            resultReceivers.add((Processor<ResultType, ?>) previousRetriever);
        }
        
        FilterCriterion<ResultType> filter = (FilterCriterion<ResultType>) filters.get(retrieverTypeIndex);
        
        Class<Processor<?, ResultType>> retrieverType = (Class<Processor<?, ResultType>>)(Class<?>) dataRetrieverTypeWithInformation.getRetrieverType();
        return createRetriever(retrieverType, retrievedDataType, resultReceivers, filter, retrieverTypeIndex);
    }

    private <ResultType> Processor<?, ResultType> createRetriever(Class<Processor<?, ResultType>> retrieverType, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter, int retrieverTypeIndex) {
        Constructor<Processor<?, ResultType>> retrieverConstructor = null;
        try {
            retrieverConstructor = retrieverType.getConstructor(ExecutorService.class, Collection.class, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given retrieverType '"
                    + retrieverType.getSimpleName() + "'", e);
        }
        
        return constructRetriever(retrieverConstructor, retrievedDataType, resultReceivers, filter, retrieverTypeIndex);
    }

    private <ResultType> Processor<?, ResultType> constructRetriever(Constructor<Processor<?, ResultType>> retrieverConstructor, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter, int retrieverTypeIndex) {
        try {
            Collection<Processor<ResultType, ?>> retrievalResultReceivers = resultReceivers;
            if (filter != null) {
                Processor<ResultType, ?> filteringProcessor = new ParallelFilteringProcessor<ResultType>(
                        retrievedDataType, executor, resultReceivers, filter);
                retrievalResultReceivers = new ArrayList<>();
                retrievalResultReceivers.add(filteringProcessor);
            }

            return retrieverConstructor.newInstance(executor, retrievalResultReceivers, retrieverTypeIndex);
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Couldn't create a data retriever instance with the constructor "
                                                    + retrieverConstructor.toString(), e);
        }
    }

}
