package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

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
    protected Runnable createInstruction(GroupedDataEntry<ElementType> element) {
        return new AbstractDirectForwardProcessingInstruction<GroupedDataEntry<ElementType>, GroupedDataEntry<FunctionReturnType>>(element, getResultReceivers()) {
            @Override
            protected GroupedDataEntry<FunctionReturnType> doWork() {
                FunctionReturnType value = extractionFunction.tryToInvoke(super.getInput().getDataEntry());
                return value != null ? new GroupedDataEntry<FunctionReturnType>(super.getInput().getKey(), value) : super.getInvalidResult();
            }
        };
    }

}
