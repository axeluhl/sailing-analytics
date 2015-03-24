package com.sap.sailing.polars.mining;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;

public abstract class AbstractEnrichingProcessor<InputType, ResultType> extends
        AbstractSimpleParallelProcessor<InputType, ResultType> {

    public AbstractEnrichingProcessor(Class<InputType> inputType, Class<ResultType> resultType,
            ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType, executor, resultReceivers);
    }

    @Override
    protected AbstractProcessorInstruction<ResultType> createInstruction(final InputType element) {
        return new AbstractProcessorInstruction<ResultType>(this) {

            @Override
            public ResultType computeResult() {
                return enrich(element);
            }
        };
    }

    protected abstract ResultType enrich(InputType element);

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
