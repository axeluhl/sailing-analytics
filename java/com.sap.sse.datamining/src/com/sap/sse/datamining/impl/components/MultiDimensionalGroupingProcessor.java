package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;

public class MultiDimensionalGroupingProcessor<DataType>
             extends AbstractSimpleParallelProcessor<DataType, GroupedDataEntry<DataType>> {

    public MultiDimensionalGroupingProcessor(Executor executor,
            Collection<Processor<GroupedDataEntry<DataType>>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Runnable createInstruction(DataType element) {
        return new AbstractDirectForwardProcessingInstruction<DataType, GroupedDataEntry<DataType>>(element, getResultReceivers()) {
            @Override
            protected GroupedDataEntry<DataType> processInput(DataType input) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

}
