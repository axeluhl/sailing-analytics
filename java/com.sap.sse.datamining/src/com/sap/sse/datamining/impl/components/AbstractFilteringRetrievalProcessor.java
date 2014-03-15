package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractFilteringRetrievalProcessor<InputType, ResultType> 
             extends AbstractPartitioningParallelProcessor<InputType, ResultType, ResultType> {

    private final FilterCriteria<ResultType> criteria;
    
    private Lock retrievedDataAmountLock;
    private int retrievedDataAmount;
    
    private Lock filteredDataAmountLock;
    private int filteredDataAmount;

    public AbstractFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<ResultType>> resultReceivers, FilterCriteria<ResultType> criteria) {
        super(executor, resultReceivers);
        this.criteria = criteria;
        retrievedDataAmountLock = new ReentrantLock();
        filteredDataAmountLock = new ReentrantLock();
    }

    @Override
    protected Iterable<ResultType> partitionElement(InputType element) {
        return retrieveData(element);
    }

    protected abstract Iterable<ResultType> retrieveData(InputType element);
    
    @Override
    protected Callable<ResultType> createInstruction(final ResultType partialElement) {
        return new Callable<ResultType>() {
            @Override
            public ResultType call() throws Exception {
                incrementRetrievedDataAmount();
                if (criteria.matches(partialElement)) {
                    incrementFilteredDataAmount();
                    return partialElement;
                }
                return createInvalidResult();
            }
        };
    }

    private void incrementRetrievedDataAmount() {
        retrievedDataAmountLock.lock();
        try {
            retrievedDataAmount++;
        } finally {
            retrievedDataAmountLock.unlock();
        }
    }

    private void incrementFilteredDataAmount() {
        filteredDataAmountLock.lock();
        try {
            filteredDataAmount++;
        } finally {
            filteredDataAmountLock.unlock();
        }
    }
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setRetrievedDataAmount(retrievedDataAmount);
        additionalDataBuilder.setFilteredDataAmount(filteredDataAmount);
    }

}
