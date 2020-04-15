package com.sap.sse.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public abstract class AbstractParallelAggregationProcessor<InputType, AggregatedType> 
                      extends AbstractParallelProcessor<InputType, AggregatedType> {

    private final String aggregationNameMessageKey;
    private final Object monitor;

    public AbstractParallelAggregationProcessor(Class<InputType> inputType,
                                                Class<AggregatedType> resultType,
                                                ExecutorService executor,
                                                Collection<Processor<AggregatedType, ?>> resultReceivers,
                                                String aggregationNameMessageKey) {
        super(inputType, resultType, executor, resultReceivers);
        this.aggregationNameMessageKey = aggregationNameMessageKey;
        monitor = new Object();
    }

    @Override
    protected ProcessorInstruction<AggregatedType> createInstruction(final InputType element) {
        if (needsSynchronization()) {
            return new AbstractProcessorInstruction<AggregatedType>(this, ProcessorInstructionPriority.Aggregation) {
                @Override
                public AggregatedType computeResult() {
                    synchronized (monitor) {
                        handleElement(element);
                    }
                    return AbstractParallelAggregationProcessor.super.createInvalidResult();
                }
            };
        } else {
            return new AbstractProcessorInstruction<AggregatedType>(this, ProcessorInstructionPriority.Aggregation) {
                @Override
                protected AggregatedType computeResult() throws Exception {
                    handleElement(element);
                    return AbstractParallelAggregationProcessor.super.createInvalidResult();
                }
                
            };
        }
    }

    /**
     * Defines if {@link #handleElement(Object) handleElement} needs to be called in a synchronized block.
     * Default is <code>true</code>.</br>
     * Override if the concrete aggregator implementation will handle the synchronization on its own. 
     * @return
     */
    protected boolean needsSynchronization() {
        return true;
    }

    /**
     * Method to handle the element. This method is only called in a way that is thread safe so
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
