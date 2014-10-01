package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;

public abstract class AbstractParallelStoringAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractSimpleParallelProcessor<InputType, AggregatedType> {

    private final Lock storeLock;
    private final String aggregationNameMessageKey;

    public AbstractParallelStoringAggregationProcessor(Class<InputType> inputType,
                                                       Class<AggregatedType> resultType,
                                                       ExecutorService executor,
                                                       Collection<Processor<AggregatedType, ?>> resultReceivers,
                                                       String aggregationNameMessageKey) {
        super(inputType, resultType, executor, resultReceivers);
        storeLock = new ReentrantLock();
        this.aggregationNameMessageKey = aggregationNameMessageKey;
    }

    @Override
    protected Callable<AggregatedType> createInstruction(final InputType element) {
        return new Callable<AggregatedType>() {
            @Override
            public AggregatedType call() throws Exception {
                storeLock.lock();
                try {
                    storeElement(element);
                } finally {
                    storeLock.unlock();
                }
                return AbstractParallelStoringAggregationProcessor.super.createInvalidResult();
            }
        };
    }

    /**
     * Method to store the element in the concrete store. This method is only called in a way, that is thread safe, so
     * that multiple threads can't corrupt the store.
     */
    protected abstract void storeElement(InputType element);
    
    @Override
    public void finish() throws InterruptedException {
        super.sleepUntilAllInstructionsFinished();
        super.forwardResultToReceivers(aggregateResult());
        super.tellResultReceiversToFinish();
    }

    protected abstract AggregatedType aggregateResult();
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setAggregationNameMessageKey(aggregationNameMessageKey);
    }

}
