package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;

public class ParallelMultiDimensionalGroupingProcessor<DataType>
             extends AbstractPartitioningParallelProcessor<Iterable<DataType>, DataType, GroupedDataEntry<DataType>> {

    public ParallelMultiDimensionalGroupingProcessor(Executor executor,
            Collection<Processor<GroupedDataEntry<DataType>>> resultReceivers, Iterable<Function<?>> dimensions) {
        super(executor, resultReceivers);
    }

    @Override
    protected Runnable createInstruction(DataType element) {
        return new AbstractDirectForwardProcessingInstruction<DataType, GroupedDataEntry<DataType>>(element, getResultReceivers()) {
            @Override
            protected GroupedDataEntry<DataType> processInput(DataType input) {
                return null;
            }
        };
    }

    @Override
    protected Iterable<DataType> partitionElement(Iterable<DataType> element) {
        return element;
    }

}
