package com.sap.sse.datamining.test.util.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.impl.components.ProcessorInstruction;

public abstract class BlockingProcessor<InputType, ResultType> extends AbstractSimpleParallelProcessor<InputType, ResultType> {
    private final long timeToBlockInMillis;

    public BlockingProcessor(Class<InputType> inputType, Class<ResultType> resultType,
                             ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers,
                             long timeToBlockInMillis) {
        super(inputType, resultType, executor, resultReceivers);
        this.timeToBlockInMillis = timeToBlockInMillis;
    }

    @Override
    protected ProcessorInstruction<ResultType> createInstruction(InputType element) {
        return new ProcessorInstruction<ResultType>(this) {
            @Override
            public ResultType computeResult() throws Exception {
                Thread.sleep(timeToBlockInMillis);
                return createResult(element);
            }
        };
    }
    
    protected abstract ResultType createResult(InputType element);

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }
    
}