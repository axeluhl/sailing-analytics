package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class ParallelMultiDimensionalGroupingProcessor<DataType>
             extends AbstractPartitioningParallelProcessor<Iterable<DataType>, DataType, GroupedDataEntry<DataType>> {

    private Iterable<Function<?>> dimensions;

    public ParallelMultiDimensionalGroupingProcessor(Executor executor, Collection<Processor<GroupedDataEntry<DataType>>> resultReceivers,
                                                     Iterable<Function<?>> dimensions) {
        super(executor, resultReceivers);
        verifyThatDimensionsAreDimensions(dimensions);
        this.dimensions = dimensions;
    }

    private void verifyThatDimensionsAreDimensions(Iterable<Function<?>> dimensions) {
        int size = 0;
        for (Function<?> possibleDimension : dimensions) {
            size++;
            if (!possibleDimension.isDimension()) {
                throw new IllegalArgumentException("The given function " + possibleDimension.toString() + " is no dimension.");
            }
        }
        
        if (size == 0) {
            throw new IllegalArgumentException("The given dimensions are empty.");
        }
    }

    @Override
    protected Runnable createInstruction(DataType element) {
        return new AbstractDirectForwardProcessingInstruction<DataType, GroupedDataEntry<DataType>>(element, getResultReceivers()) {
            @Override
            protected GroupedDataEntry<DataType> doWork() {
                DataType input = super.getInput();
                return new GroupedDataEntry<DataType>(createCompoundKeyFor(input, dimensions.iterator()), input);
            }
        };
    }

    private GroupKey createCompoundKeyFor(DataType input, Iterator<Function<?>> dimensionsIterator) {
        Function<?> mainDimension = dimensionsIterator.next();
        GroupKey key = createGroupKeyFor(input, mainDimension);
        if (dimensionsIterator.hasNext()) {
            key = new CompoundGroupKey(key, createCompoundKeyFor(input, dimensionsIterator));
        }
        return key;
    }

    private GroupKey createGroupKeyFor(DataType input, Function<?> mainDimension) {
        Object keyValue = mainDimension.tryToInvoke(input);
        return new GenericGroupKey<Object>(keyValue);
    }

    @Override
    protected Iterable<DataType> partitionElement(Iterable<DataType> element) {
        return element;
    }

}
