package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public class ParallelFilteringProcessor<InputType> extends AbstractSimpleParallelProcessor<InputType, InputType> {

    private final FilterCriteria<InputType> filterCriteria;
    
    private int filteredDataAmount;

    public ParallelFilteringProcessor(ExecutorService executor, Collection<Processor<InputType>> resultReceivers, FilterCriteria<InputType> filterCriteria) {
        super(executor, resultReceivers);
        this.filterCriteria = filterCriteria;
        filteredDataAmount = 0;
    }

    @Override
    protected Callable<InputType> createInstruction(final InputType element) {
        return new Callable<InputType>() {
            @Override
            public InputType call() throws Exception {
                if (filterCriteria.matches(element)) {
                    increaseFilteredDataAmount();
                    return element;
                } else {
                    return ParallelFilteringProcessor.super.createInvalidResult();
                }
            }
        };
    }

    private synchronized void increaseFilteredDataAmount() {
        filteredDataAmount++;
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setFilteredDataAmount(filteredDataAmount);
    }

}
