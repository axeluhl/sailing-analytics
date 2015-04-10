package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractSimpleRetrievalProcessor<InputType, ResultType> extends
        AbstractRetrievalProcessor<InputType, ResultType, ResultType> {

    /**
     * 
     * @param inputType
     * @param resultType
     * @param executor
     * @param resultReceivers
     * @param retrievalLevel The position of this retriever in it's chain. <code>0</code> represents the first.
     */
    public AbstractSimpleRetrievalProcessor(Class<InputType> inputType, Class<ResultType> resultType,
            ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers, int retrievalLevel) {
        super(inputType, resultType, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected ResultType convertWorkingToResultType(ResultType partialElement) {
        return partialElement;
    }

}
