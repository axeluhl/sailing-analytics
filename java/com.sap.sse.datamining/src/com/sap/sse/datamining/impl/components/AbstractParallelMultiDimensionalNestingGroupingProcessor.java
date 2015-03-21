package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.NestingCompoundGroupKey;

public abstract class AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType>
                      extends AbstractSimpleParallelProcessor<DataType, GroupedDataEntry<DataType>> {

    private Iterable<Pair<Function<?>, ParameterProvider>> dimensionsWithParameterProvider;

    @SuppressWarnings("unchecked")
    public AbstractParallelMultiDimensionalNestingGroupingProcessor(Class<DataType> dataType,
                                                             ExecutorService executor,
                                                             Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                             Iterable<Pair<Function<?>, ParameterProvider>> dimensionsWithParameterProvider) {
        super(dataType, (Class<GroupedDataEntry<DataType>>)(Class<?>) GroupedDataEntry.class, executor, resultReceivers);
        verifyThatDimensionsAreDimensions(dimensionsWithParameterProvider);
        this.dimensionsWithParameterProvider = dimensionsWithParameterProvider;
    }

    private void verifyThatDimensionsAreDimensions(Iterable<Pair<Function<?>, ParameterProvider>> dimensionsWithParameterProvider) {
        if (dimensionsWithParameterProvider == null) {
            throw new IllegalArgumentException("The given dimensions mustn't be null.");
        }
        
        int size = 0;
        for (Pair<Function<?>, ParameterProvider> possibleDimension : dimensionsWithParameterProvider) {
            size++;
            if (!possibleDimension.getA().isDimension()) {
                throw new IllegalArgumentException("The given function " + possibleDimension.getA().toString() + " is no dimension.");
            }
        }
        
        if (size == 0) {
            throw new IllegalArgumentException("The given dimensions are empty.");
        }
    }

    @Override
    protected ProcessorInstruction<GroupedDataEntry<DataType>> createInstruction(final DataType element) {
        return new ProcessorInstruction<GroupedDataEntry<DataType>>(this, ProcessorInstructionPriority.Grouping) {
            @Override
            public GroupedDataEntry<DataType> computeResult() {
                return new GroupedDataEntry<DataType>(createCompoundKeyFor(element,
                        dimensionsWithParameterProvider.iterator()), element);
            }
        };
    }
    
    private GroupKey createCompoundKeyFor(DataType input, Iterator<Pair<Function<?>, ParameterProvider>> dimensionsIterator) {
        Pair<Function<?>, ParameterProvider> mainDimension = dimensionsIterator.next();
        GroupKey key = createGroupKeyFor(input, mainDimension.getA(), mainDimension.getB());
        if (dimensionsIterator.hasNext()) {
            key = new NestingCompoundGroupKey(key, createCompoundKeyFor(input, dimensionsIterator));
        }
        return key;
    }

    protected abstract GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider);
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
