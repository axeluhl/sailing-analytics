package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

/**
 * A grouper that takes multiple dimensions (as {@link Function Functions}) and groups the given elements
 * by the values of the dimensions.<br>
 * A data element of the type <code>T</code> with the dimension values <code>a</code>, <code>b</code> and
 * <code>c</code> would be grouped with the key <code>{@literal a<b<c>>}</code>.<br>
 * The order of the given dimensions determines the order of the key nesting.
 * 
 * @author Lennart Hensler (D054527)
 * 
 * @see com.sap.sse.datamining.impl.components com.sap.sse.datamining.impl.components for general information about Processors.
 */
public class ParallelMultiDimensionsValueNestingGroupingProcessor<DataType>
             extends AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType> {

    /**
     * @throws IllegalArgumentException if any of the given function isn't a dimension
     *                                  or the given iterable is empty.
     */
    public ParallelMultiDimensionsValueNestingGroupingProcessor(Class<DataType> dataType,
                                                                ExecutorService executor,
                                                                Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                                Iterable<ParameterizedFunction<?>> parameterizedDimensions) {
        super(dataType, executor, resultReceivers, parameterizedDimensions);
    }

    protected GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider) {
        Object keyValue = dimension.tryToInvoke(input, parameterProvider);
        return new GenericGroupKey<Object>(keyValue);
    }

}
