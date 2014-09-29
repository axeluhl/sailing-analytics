package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

/**
 * Takes a dimension and groups the given elements by the dimensions {@link FunctionDTO}.
 * 
 * @author Lennart Hensler (D054527)
 */
public class ParallelByDimensionGroupingProcessor<DataType> extends
        AbstractParallelMultiDimensionalGroupingProcessor<DataType> {

    public ParallelByDimensionGroupingProcessor(Class<DataType> dataType,
                                                ExecutorService executor,
                                                Collection<Processor<GroupedDataEntry<DataType>>> resultReceivers,
                                                Function<?> dimension) {
        super(dataType, executor, resultReceivers, asIterable(dimension));
    }

    private static Iterable<Function<?>> asIterable(Function<?> dimension) {
        Collection<Function<?>> collection = new ArrayList<>();
        collection.add(dimension);
        return collection;
    }

    @Override
    protected GroupKey createGroupKeyFor(DataType input, Function<?> dimension) {
        return new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimension));
    }

}
