package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractFilteringRetrievalProcessor<InputType, ResultType, ResultTypeWithContext> 
             extends AbstractPartitioningParallelProcessor<InputType, ResultType, ResultTypeWithContext> {

    private final FilterCriteria<ResultTypeWithContext> criteria;
    
    private Lock retrievedDataAmountLock;
    private int retrievedDataAmount;
    
    private Lock filteredDataAmountLock;
    private int filteredDataAmount;

    public AbstractFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<ResultTypeWithContext>> resultReceivers, FilterCriteria<ResultTypeWithContext> criteria) {
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
    protected Callable<ResultTypeWithContext> createInstruction(final ResultType partialElement) {
        return new Callable<ResultTypeWithContext>() {
            @Override
            public ResultTypeWithContext call() throws Exception {
                incrementRetrievedDataAmount();
                ResultTypeWithContext elementWithContext = contextifyElement(partialElement);
                if (criteria.matches(elementWithContext)) {
                    incrementFilteredDataAmount();
                    return elementWithContext;
                }
                return createInvalidResult();
            }
        };
    }

    protected abstract ResultTypeWithContext contextifyElement(ResultType partialElement);

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
