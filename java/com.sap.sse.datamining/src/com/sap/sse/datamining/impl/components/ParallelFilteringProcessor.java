package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public class ParallelFilteringProcessor<InputType> extends AbstractSimpleParallelProcessor<InputType, InputType> {

    private final FilterCriterion<InputType> filterCriterion;
    
    private final Lock filteredDataAmountLock;
    private int filteredDataAmount;

    public ParallelFilteringProcessor(Class<InputType> inputType, ExecutorService executor, Collection<Processor<InputType, ?>> resultReceivers, FilterCriterion<InputType> filterCriterion) {
        super(inputType, inputType, executor, resultReceivers);
        this.filterCriterion = filterCriterion;
        filteredDataAmountLock = new ReentrantLock();
    }

    @Override
    protected AbstractProcessorInstruction<InputType> createInstruction(final InputType element) {
        return new AbstractProcessorInstruction<InputType>(this, ProcessorInstructionPriority.Filtration) {
            @Override
            public InputType computeResult() {
                if (filterCriterion.matches(element)) {
                    return element;
                } else {
                    incrementFilteredDataAmount();
                    return ParallelFilteringProcessor.super.createInvalidResult();
                }
            }
        };
    }

    private void incrementFilteredDataAmount() {
        filteredDataAmountLock.lock();
        try {
            filteredDataAmount++;
        } finally {
            filteredDataAmountLock.unlock();
        }
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        int retrievedDataAmount = additionalDataBuilder.getRetrievedDataAmount();
        additionalDataBuilder.setRetrievedDataAmount(retrievedDataAmount - filteredDataAmount);
    }

}
