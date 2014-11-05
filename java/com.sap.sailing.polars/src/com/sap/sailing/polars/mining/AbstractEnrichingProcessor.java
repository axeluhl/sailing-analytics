package com.sap.sailing.polars.mining;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;

public abstract class AbstractEnrichingProcessor<InputType, ResultType> extends
        AbstractSimpleParallelProcessor<InputType, ResultType> {

    public AbstractEnrichingProcessor(Class<InputType> inputType, Class<ResultType> resultType,
            ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType, executor, resultReceivers);
    }

    @Override
    protected Callable<ResultType> createInstruction(final InputType element) {
        return new Callable<ResultType>() {

            @Override
            public ResultType call() throws Exception {
                return enrich(element);
            }
        };
    }

    protected abstract ResultType enrich(InputType element);

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
