package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractRetrievalProcessor<InputType, WorkingType, ResultType> extends
        AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType> {

    private final Lock retrievedDataAmountLock;
    private int retrievedDataAmount;

    public AbstractRetrievalProcessor(Class<InputType> inputType, Class<ResultType> resultType, ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType, executor, resultReceivers);
        retrievedDataAmountLock = new ReentrantLock(); 
    }

    @Override
    protected Callable<ResultType> createInstruction(final WorkingType partialElement) {
        return new Callable<ResultType>() {
            @Override
            public ResultType call() throws Exception {
                incrementRetrievedDataAmount();
                return convertWorkingToResultType(partialElement);
            }
        };
    }

    protected abstract ResultType convertWorkingToResultType(WorkingType partialElement);

    // Override, to provide a better method name to the sub classes
    @Override
    protected Iterable<WorkingType> partitionElement(InputType element) {
        return retrieveData(element);
    }

    protected abstract Iterable<WorkingType> retrieveData(InputType element);

    private void incrementRetrievedDataAmount() {
        retrievedDataAmountLock.lock();
        try {
            retrievedDataAmount++;
        } finally {
            retrievedDataAmountLock.unlock();
        }
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setRetrievedDataAmount(retrievedDataAmount);
    }

}
