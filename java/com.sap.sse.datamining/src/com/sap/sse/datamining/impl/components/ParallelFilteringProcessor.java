package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public class ParallelFilteringProcessor<InputType> extends AbstractSimpleParallelProcessor<InputType, InputType> {

    private final FilterCriteria<InputType> filterCriteria;

    public ParallelFilteringProcessor(Executor executor, Collection<Processor<InputType>> resultReceivers, FilterCriteria<InputType> filterCriteria) {
        super(executor, resultReceivers);
        this.filterCriteria = filterCriteria;
    }

    @Override
    protected Callable<InputType> createInstruction(final InputType element) {
        return new Callable<InputType>() {
            @Override
            public InputType call() throws Exception {
                InputType input = element;
                return filterCriteria.matches(input) ? input : ParallelFilteringProcessor.super.createInvalidResult();
            }
        };
    }

}
