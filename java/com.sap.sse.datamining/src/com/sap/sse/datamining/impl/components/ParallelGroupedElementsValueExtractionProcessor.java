package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;

public class ParallelGroupedElementsValueExtractionProcessor<ElementType, FunctionReturnType>
             extends AbstractSimpleParallelProcessor<GroupedDataEntry<ElementType>, GroupedDataEntry<FunctionReturnType>> {

    private final Function<FunctionReturnType> extractionFunction;
    
    public ParallelGroupedElementsValueExtractionProcessor(ThreadPoolExecutor executor,
            Collection<Processor<GroupedDataEntry<FunctionReturnType>>> resultReceivers,
            Function<FunctionReturnType> extractionFunction) {
        super(executor, resultReceivers);
        this.extractionFunction = extractionFunction;
    }

    @Override
    protected Callable<GroupedDataEntry<FunctionReturnType>> createInstruction(final GroupedDataEntry<ElementType> element) {
        return new Callable<GroupedDataEntry<FunctionReturnType>>() {
            @Override
            public GroupedDataEntry<FunctionReturnType> call() throws Exception {
                FunctionReturnType value = extractionFunction.tryToInvoke(element.getDataEntry());
                return value != null ? new GroupedDataEntry<FunctionReturnType>(element.getKey(), value) :
                                       ParallelGroupedElementsValueExtractionProcessor.super.createInvalidResult();
            }
        };
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setExtractedStatisticNameMessageKey(extractionFunction.getMessageKey());
    }

}
