package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractSimpleFilteringRetrievalProcessor<InputType, ResultType> extends
        AbstractFilteringRetrievalProcessor<InputType, ResultType, ResultType> {

    public AbstractSimpleFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<ResultType>> resultReceivers, FilterCriterion<ResultType> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected ResultType convertWorkingToResultType(ResultType partialElement) {
        return partialElement;
    }

}
