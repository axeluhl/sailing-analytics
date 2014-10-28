package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;

public abstract class AbstractParallelMultiDimensionalGroupingProcessor<DataType>
                      extends AbstractSimpleParallelProcessor<DataType, GroupedDataEntry<DataType>> {

    private Iterable<Function<?>> dimensions;

    @SuppressWarnings("unchecked")
    public AbstractParallelMultiDimensionalGroupingProcessor(Class<DataType> dataType,
                                                             ExecutorService executor,
                                                             Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                             Iterable<Function<?>> dimensions) {
        super(dataType, (Class<GroupedDataEntry<DataType>>)(Class<?>) GroupedDataEntry.class, executor, resultReceivers);
        verifyThatDimensionsAreDimensions(dimensions);
        this.dimensions = dimensions;
    }

    private void verifyThatDimensionsAreDimensions(Iterable<Function<?>> dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("The given dimensions mustn't be null.");
        }
        
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
    protected Callable<GroupedDataEntry<DataType>> createInstruction(final DataType element) {
        return new Callable<GroupedDataEntry<DataType>>() {
            @Override
            public GroupedDataEntry<DataType> call() throws Exception {
                return new GroupedDataEntry<DataType>(createCompoundKeyFor(element, dimensions.iterator()), element);
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

    protected abstract GroupKey createGroupKeyFor(DataType input, Function<?> dimension);

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
