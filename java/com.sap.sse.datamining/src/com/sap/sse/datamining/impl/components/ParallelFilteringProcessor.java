package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public class ParallelFilteringProcessor<InputType> extends AbstractSimpleParallelProcessor<InputType, InputType> {

    private final FilterCriterion<InputType> filterCriteria;

    public ParallelFilteringProcessor(Class<InputType> inputType, ExecutorService executor, Collection<Processor<InputType, ?>> resultReceivers, FilterCriterion<InputType> filterCriteria) {
        super(inputType, inputType, executor, resultReceivers);
        this.filterCriteria = filterCriteria;
    }

    @Override
    protected Callable<InputType> createInstruction(final InputType element) {
        return new Callable<InputType>() {
            @Override
            public InputType call() throws Exception {
                if (filterCriteria.matches(element)) {
                    return element;
                } else {
                    return ParallelFilteringProcessor.super.createInvalidResult();
                }
            }
        };
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
