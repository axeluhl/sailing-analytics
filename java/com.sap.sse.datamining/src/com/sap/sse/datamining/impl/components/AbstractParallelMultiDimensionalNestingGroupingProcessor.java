package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.NestingCompoundGroupKey;

public abstract class AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType>
                      extends AbstractParallelProcessor<DataType, GroupedDataEntry<DataType>> {

    private Iterable<ParameterizedFunction<?>> parameterizedDimensions;

    @SuppressWarnings("unchecked")
    public AbstractParallelMultiDimensionalNestingGroupingProcessor(Class<DataType> dataType,
                                                             ExecutorService executor,
                                                             Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                             Iterable<ParameterizedFunction<?>> parameterizedDimensions) {
        super(dataType, (Class<GroupedDataEntry<DataType>>)(Class<?>) GroupedDataEntry.class, executor, resultReceivers);
        verifyThatDimensionsAreDimensions(parameterizedDimensions);
        this.parameterizedDimensions = parameterizedDimensions;
    }

    private void verifyThatDimensionsAreDimensions(Iterable<ParameterizedFunction<?>> parameterizedDimensions) {
        if (parameterizedDimensions == null) {
            throw new IllegalArgumentException("The given dimensions mustn't be null.");
        }
        
        int size = 0;
        for (ParameterizedFunction<?> possibleParameterizedDimension : parameterizedDimensions) {
            size++;
            if (!possibleParameterizedDimension.getFunction().isDimension()) {
                throw new IllegalArgumentException("The given function " + possibleParameterizedDimension.getFunction().toString() + " is no dimension.");
            }
        }
        
        if (size == 0) {
            throw new IllegalArgumentException("The given dimensions are empty.");
        }
    }

    @Override
    protected AbstractProcessorInstruction<GroupedDataEntry<DataType>> createInstruction(final DataType element) {
        return new AbstractProcessorInstruction<GroupedDataEntry<DataType>>(this, ProcessorInstructionPriority.Grouping) {
            @Override
            public GroupedDataEntry<DataType> computeResult() {
                return new GroupedDataEntry<DataType>(createCompoundKeyFor(element, parameterizedDimensions.iterator()), element);
            }
        };
    }
    
    private GroupKey createCompoundKeyFor(DataType input, Iterator<ParameterizedFunction<?>> dimensionsIterator) {
        ParameterizedFunction<?> mainDimension = dimensionsIterator.next();
        GroupKey key = createGroupKeyFor(input, mainDimension.getFunction(), mainDimension.getParameterProvider());
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
