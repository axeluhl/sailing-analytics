package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

/**
 * Takes a dimension and groups the given elements by the dimensions {@link FunctionDTO}.
 * 
 * @author Lennart Hensler (D054527)
 */
public class ParallelByDimensionGroupingProcessor<DataType> extends
        AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType> {

    private final DataMiningStringMessages stringMessages;
    private final Locale locale;

    private final FunctionDTOFactory functionDTOFactory;
    
    public ParallelByDimensionGroupingProcessor(Class<DataType> dataType,
                                                ExecutorService executor,
                                                Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                Function<?> dimension,
                                                DataMiningStringMessages stringMessages, Locale locale) {
        super(dataType, executor, resultReceivers, asIterable(dimension));
        this.stringMessages = stringMessages;
        this.locale = locale;
        
        functionDTOFactory = new FunctionDTOFactory();
    }

    private static Iterable<Pair<Function<?>, ParameterProvider>> asIterable(Function<?> dimension) {
        Collection<Pair<Function<?>, ParameterProvider>> collection = new ArrayList<>();
        collection.add(new Pair<Function<?>, ParameterProvider>(dimension, ParameterProvider.NULL));
        return collection;
    }

    @Override
    protected GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider) {
        return new GenericGroupKey<FunctionDTO>(functionDTOFactory.createFunctionDTO(dimension, stringMessages, locale));
    }

}
