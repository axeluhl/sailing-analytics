package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public class ParallelFilteringProcessor<InputType> extends AbstractParallelProcessor<InputType, InputType> {

    private final FilterCriterion<InputType> filterCriterion;
    private final AtomicInteger filteredDataAmount;

    public ParallelFilteringProcessor(Class<InputType> inputType, ExecutorService executor, Collection<Processor<InputType, ?>> resultReceivers, FilterCriterion<InputType> filterCriterion) {
        super(inputType, inputType, executor, resultReceivers);
        this.filterCriterion = filterCriterion;
        filteredDataAmount = new AtomicInteger();
    }

    @Override
    protected AbstractProcessorInstruction<InputType> createInstruction(final InputType element) {
        return new AbstractProcessorInstruction<InputType>(this, ProcessorInstructionPriority.Filtration) {
            @Override
            public InputType computeResult() {
                if (filterCriterion.matches(element)) {
                    return element;
                } else {
                    filteredDataAmount.incrementAndGet();
                    return ParallelFilteringProcessor.super.createInvalidResult();
                }
            }
        };
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        int retrievedDataAmount = additionalDataBuilder.getRetrievedDataAmount();
        additionalDataBuilder.setRetrievedDataAmount(retrievedDataAmount - filteredDataAmount.get());
    }

}
