package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.impl.components.AbstractDirectForwardProcessingInstruction;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;

public class ParallelFilteringProcessor<InputType> extends AbstractSimpleParallelProcessor<InputType, InputType> {

    private final FilterCriteria<InputType> filterCriteria;

    public ParallelFilteringProcessor(Executor executor, Collection<Processor<InputType>> resultReceivers, FilterCriteria<InputType> filterCriteria) {
        super(executor, resultReceivers);
        this.filterCriteria = filterCriteria;
    }

    @Override
    protected Runnable createInstruction(InputType element) {
        return new AbstractDirectForwardProcessingInstruction<InputType, InputType>(element, getResultReceivers()) {
            @Override
            protected InputType doWork() {
                InputType input = super.getInput();
                return filterCriteria.matches(input) ? input : super.getInvalidResult();
            }
        };
    }

}
