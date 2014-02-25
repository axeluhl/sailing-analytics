package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractStoringParallelAggregationProcessor<InputType, AggregatedType> extends
        AbstractSimpleParallelProcessor<InputType, AggregatedType> {

    public AbstractStoringParallelAggregationProcessor(Executor executor, Collection<Processor<AggregatedType>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Callable<AggregatedType> createInstruction(final InputType element) {
        return new Callable<AggregatedType>() {
            @Override
            public AggregatedType call() throws Exception {
                storeElement(element);
                return AbstractStoringParallelAggregationProcessor.super.createInvalidResult();
            }
        };
    }

    protected abstract void storeElement(InputType element);
    
    @Override
    public void finish() throws InterruptedException {
        super.forwardResultToReceivers(aggregateResult());
        super.finish();
    }

    protected abstract AggregatedType aggregateResult();

}
