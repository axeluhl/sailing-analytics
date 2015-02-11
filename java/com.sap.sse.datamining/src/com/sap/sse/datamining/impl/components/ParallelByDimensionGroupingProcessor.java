package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Takes a dimension and groups the given elements by the dimensions {@link FunctionDTO}.
 * 
 * @author Lennart Hensler (D054527)
 */
public class ParallelByDimensionGroupingProcessor<DataType> extends
        AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType> {

    private final ResourceBundleStringMessages stringMessages;
    private final Locale locale;

    private final FunctionDTOFactory functionDTOFactory;
    
    public ParallelByDimensionGroupingProcessor(Class<DataType> dataType,
                                                ExecutorService executor,
                                                Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                Function<?> dimension,
                                                ResourceBundleStringMessages stringMessages, Locale locale) {
        super(dataType, executor, resultReceivers, asIterable(dimension));
        this.stringMessages = stringMessages;
        this.locale = locale;
        
        functionDTOFactory = new FunctionDTOFactory();
    }

    private static Iterable<Function<?>> asIterable(Function<?> dimension) {
        Collection<Function<?>> collection = new ArrayList<>();
        collection.add(dimension);
        return collection;
    }

    @Override
    protected GroupKey createGroupKeyFor(DataType input, Function<?> dimension) {
        return new GenericGroupKey<FunctionDTO>(functionDTOFactory.createFunctionDTO(dimension, stringMessages, locale));
    }

}
