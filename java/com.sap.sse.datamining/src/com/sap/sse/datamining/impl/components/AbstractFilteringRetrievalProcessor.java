package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractFilteringRetrievalProcessor<InputType, WorkingType, ResultType> 
             extends AbstractRetrievalProcessor<InputType, WorkingType, ResultType> {

    private final FilterCriteria<ResultType> criteria;

    public AbstractFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<ResultType>> resultReceivers, FilterCriteria<ResultType> criteria) {
        super(executor, resultReceivers);
        this.criteria = criteria;
    }
    
    @Override
    protected Callable<ResultType> createInstruction(final WorkingType partialElement) {
        final Callable<ResultType> superInstruction = super.createInstruction(partialElement);
        return new Callable<ResultType>() {
            @Override
            public ResultType call() throws Exception {
                ResultType elementWithContext = superInstruction.call();
                if (criteria.matches(elementWithContext)) {
                    return elementWithContext;
                } else {
                    AbstractFilteringRetrievalProcessor.super.decrementRetrievedDataAmount();
                    return createInvalidResult();
                }
            }
        };
    }

}
