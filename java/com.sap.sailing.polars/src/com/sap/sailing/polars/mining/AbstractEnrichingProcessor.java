package com.sap.sailing.polars.mining;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;

public abstract class AbstractEnrichingProcessor<InputType, ResultType> extends AbstractParallelProcessor<InputType, ResultType> {

    public AbstractEnrichingProcessor(Class<InputType> inputType, Class<ResultType> resultType,
            ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType, executor, resultReceivers);
    }

    @Override
    protected ProcessorInstruction<ResultType> createInstruction(final InputType element) {
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
