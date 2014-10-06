package com.sap.sse.datamining.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.criterias.NonFilteringFilterCriterion;

public class SimpleDataRetrieverChainBuilder<DataSourceType> implements DataRetrieverChainBuilder<DataSourceType> {
    
    private final ExecutorService executor;
    private final List<DataRetrieverTypeWithInformation<?, ?>> dataRetrieverTypesWithInformation;

    private final TypeSafeFilterCriteriaCollection filters;
    private final TypeSafeResultReceiverCollection receivers;
    private int currentRetrieverTypeIndex;

    /**
     * Creates a data retriever chain builder for the given list of {@link DataRetrieverTypeWithInformation}.</br>
     * The list has the match the following conditions to build a valid data retriever chain:
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
        
        filters = new TypeSafeFilterCriteriaCollection();
        receivers = new TypeSafeResultReceiverCollection();
        currentRetrieverTypeIndex = 0;
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
    public <T> DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<T, ?> resultReceiver) {
        Class<?> currentRetrievedDataType = getCurrentRetrievedDataType();
        if (!resultReceiver.getInputType().isAssignableFrom(currentRetrievedDataType)) {
            throw new IllegalArgumentException("The given result receiver (with input type '" + resultReceiver.getInputType().getSimpleName()
                    + "') isn't able to process the current retrieved data type '" + currentRetrievedDataType.getSimpleName() + "'");
        }
        
        receivers.addResultReceiver((Class<T>) getCurrentRetrievedDataType(), resultReceiver);
        return this;
    }
    
    @Override
    public boolean canStepDeeper() {
        return currentRetrieverTypeIndex + 1 < dataRetrieverTypesWithInformation.size();
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> stepDeeper() {
        currentRetrieverTypeIndex++;
        return this;
    }

    @Override
    public Class<?> getCurrentRetrievedDataType() {
        return dataRetrieverTypesWithInformation.get(currentRetrieverTypeIndex).getRetrievedDataType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Processor<DataSourceType, ?> build() {
        Processor<?, ?> firstRetriever = null;
        for (int retrievedDataTypeIndex = currentRetrieverTypeIndex; retrievedDataTypeIndex >= 0; retrievedDataTypeIndex--) {
            DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation = dataRetrieverTypesWithInformation.get(retrievedDataTypeIndex);
            firstRetriever = createRetriever(dataRetrieverTypeWithInformation, firstRetriever);
        }
        
        return (Processor<DataSourceType, ?>) firstRetriever;
    }
    
    @SuppressWarnings("unchecked")
    private <ResultType> Processor<?, ResultType> createRetriever(DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation, Processor<?, ?> previousRetriever) {
        Class<ResultType> retrievedDataType = (Class<ResultType>) dataRetrieverTypeWithInformation.getRetrievedDataType();
        
        Collection<?> storedResultReceivers = receivers.getResultReceivers(retrievedDataType);
        Collection<Processor<ResultType, ?>> resultReceivers = storedResultReceivers != null ? new ArrayList<Processor<ResultType, ?>>((Collection<Processor<ResultType, ?>>) storedResultReceivers) : new ArrayList<Processor<ResultType, ?>>();
        if (previousRetriever != null) {
            resultReceivers.add((Processor<ResultType, ?>) previousRetriever);
        }
        
        FilterCriterion<ResultType> filter = filters.getCriterion(retrievedDataType);
        
        Class<Processor<?, ResultType>> retrieverType = (Class<Processor<?, ResultType>>)(Class<?>) dataRetrieverTypeWithInformation.getRetrieverType();
        return createRetriever(retrieverType, retrievedDataType, resultReceivers, filter);
    }

    private <ResultType> Processor<?, ResultType> createRetriever(Class<Processor<?, ResultType>> retrieverType, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter) {
        Constructor<Processor<?, ResultType>> retrieverConstructor = null;
        try {
            retrieverConstructor = retrieverType.getConstructor(ExecutorService.class, Collection.class);
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Remove the inner try-catch, after all filtering retriever have been split up to a retriever
            //      followed by a filter
            try {
                retrieverConstructor = retrieverType.getConstructor(ExecutorService.class, Collection.class, FilterCriterion.class);
            } catch (NoSuchMethodException | SecurityException e1) {
                throw new IllegalArgumentException("Couldn't get an usable constructor from the given retrieverType '"
                        + retrieverType.getSimpleName() + "'", e);
            }
        }
        
        return constructRetriever(retrieverConstructor, retrievedDataType, resultReceivers, filter);
    }

    private <ResultType> Processor<?, ResultType> constructRetriever(Constructor<Processor<?, ResultType>> retrieverConstructor, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter) {
        try {
            if (retrieverConstructor.getParameterTypes().length == 2) {
                Collection<Processor<ResultType, ?>> retrievalResultReceivers = resultReceivers;
                if (filter != null) {
                    Processor<ResultType, ?> filteringProcessor = new ParallelFilteringProcessor<ResultType>(retrievedDataType, executor, resultReceivers, filter);
                    retrievalResultReceivers = new ArrayList<>();
                    retrievalResultReceivers.add(filteringProcessor);
                }
                
                return retrieverConstructor.newInstance(executor, retrievalResultReceivers);
            }
            // TODO Remove the second if, after all filtering retriever have been split up to a retriever
            //      followed by a filter
            if (retrieverConstructor.getParameterTypes().length == 3) {
                if (filter == null) {
                    filter = new NonFilteringFilterCriterion<>(retrievedDataType);
                }
                return retrieverConstructor.newInstance(executor, resultReceivers, filter);
            }
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Couldn't create a data retriever instance with the constructor "
                                                    + retrieverConstructor.toString(), e);
        }
        
        throw new UnsupportedOperationException("Couldn't create a data retriever instance with the constructor "
                                                + retrieverConstructor.toString());
    }

}
