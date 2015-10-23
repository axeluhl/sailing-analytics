package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;

public abstract class AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType>
                      extends AbstractParallelProcessor<DataType, GroupedDataEntry<DataType>> {

    private Iterable<ParameterizedFunction<?>> parameterizedDimensions;

    /**
     * @throws IllegalArgumentException if any of the given function isn't a dimension
     *                                  or the given iterable is empty.
     */
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
    protected ProcessorInstruction<GroupedDataEntry<DataType>> createInstruction(final DataType element) {
        return new AbstractProcessorInstruction<GroupedDataEntry<DataType>>(this, ProcessorInstructionPriority.Grouping) {
            @Override
            public GroupedDataEntry<DataType> computeResult() {
                return new GroupedDataEntry<DataType>(createCompoundKeyFor(element, parameterizedDimensions.iterator()), element);
            }
        };
    }
    
    private GroupKey createCompoundKeyFor(DataType input, Iterator<ParameterizedFunction<?>> dimensionsIterator) {
        ParameterizedFunction<?> mainDimension = dimensionsIterator.next();
        GroupKey mainKey = createGroupKeyFor(input, mainDimension.getFunction(), mainDimension.getParameterProvider());
        List<GroupKey> subKeys = new ArrayList<>();
        if (dimensionsIterator.hasNext()) {
            while (dimensionsIterator.hasNext()) {
                ParameterizedFunction<?> dimension = dimensionsIterator.next();
                subKeys.add(createGroupKeyFor(input, dimension.getFunction(), dimension.getParameterProvider()));
            }
        }
        return subKeys.isEmpty() ? mainKey : new CompoundGroupKey(mainKey, subKeys);
    }

    protected abstract GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider);
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
