package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public abstract class AbstractParallelAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractParallelProcessor<InputType, AggregatedType> {

    private final String aggregationNameMessageKey;
    private final Lock writeLock;

    public AbstractParallelAggregationProcessor(Class<InputType> inputType,
                                                Class<AggregatedType> resultType,
                                                ExecutorService executor,
                                                Collection<Processor<AggregatedType, ?>> resultReceivers,
                                                String aggregationNameMessageKey) {
        super(inputType, resultType, executor, resultReceivers);
        this.aggregationNameMessageKey = aggregationNameMessageKey;
        writeLock = new ReentrantLock();
    }

    @Override
    protected AbstractProcessorInstruction<AggregatedType> createInstruction(final InputType element) {
        return new AbstractProcessorInstruction<AggregatedType>(this, ProcessorInstructionPriority.Aggregation) {
            @Override
            public AggregatedType computeResult() {
                writeLock.lock();
                try {
                    handleElement(element);
                } finally {
                    writeLock.unlock();
                }
                return AbstractParallelAggregationProcessor.super.createInvalidResult();
            }
        };
    }


    /**
     * Method to handle the element. This method is only called in a way, that is thread safe, so
     * that multiple threads can't corrupt the data.
     */
    protected abstract void handleElement(InputType element);
    
    @Override
    public void finish() throws InterruptedException {
        super.sleepUntilAllInstructionsFinished();
        super.forwardResultToReceivers(getResult());
        super.tellResultReceiversToFinish();
    }

    protected abstract AggregatedType getResult();
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setAggregationNameMessageKey(aggregationNameMessageKey);
    }

}
