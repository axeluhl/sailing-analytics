package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util;
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
                return new GroupedDataEntry<DataType>(createGroupKeyFor(element), element);
            }
        };
    }
    
    private GroupKey createGroupKeyFor(DataType input) {
        if (Util.size(parameterizedDimensions) == 1) {
            ParameterizedFunction<?> parameterizedDimension = Util.get(parameterizedDimensions, 0);
            return createGroupKeyFor(input, parameterizedDimension.getFunction(), parameterizedDimension.getParameterProvider());
        } else {
            List<GroupKey> keys = new ArrayList<>();
            for (ParameterizedFunction<?> parameterizedDimension : parameterizedDimensions) {
                if (isAborted()) {
                    break;
                }
                keys.add(createGroupKeyFor(input, parameterizedDimension.getFunction(), parameterizedDimension.getParameterProvider()));
            }
            return new CompoundGroupKey(keys);
        }
    }

    protected abstract GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider);
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
    }

}
