package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;

public abstract class AbstractStoringParallelAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractSimpleParallelProcessor<InputType, AggregatedType> {

    private final ReentrantReadWriteLock storeLock;

    public AbstractStoringParallelAggregationProcessor(Executor executor, Collection<Processor<AggregatedType>> resultReceivers) {
        super(executor, resultReceivers);
        storeLock = new ReentrantReadWriteLock();
    }

    @Override
    protected Callable<AggregatedType> createInstruction(final InputType element) {
        return new Callable<AggregatedType>() {
            @Override
            public AggregatedType call() throws Exception {
                storeLock.writeLock().lock();
                try {
                    storeElement(element);
                } finally {
                    storeLock.writeLock().unlock();
                }
                return AbstractStoringParallelAggregationProcessor.super.createInvalidResult();
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
        super.notifyResultReceiversToFinish();
    }

    protected abstract AggregatedType aggregateResult();

}
