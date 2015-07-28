package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.DataMiningDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
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

    private final DataMiningDTOFactory dtoFactory;
    
    public ParallelByDimensionGroupingProcessor(Class<DataType> dataType,
                                                ExecutorService executor,
                                                Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
                                                ParameterizedFunction<?> parameterizedDimension,
                                                ResourceBundleStringMessages stringMessages, Locale locale) {
        super(dataType, executor, resultReceivers, asIterable(parameterizedDimension));
        this.stringMessages = stringMessages;
        this.locale = locale;
        
        dtoFactory = new DataMiningDTOFactory();
    }

    private static Iterable<ParameterizedFunction<?>> asIterable(ParameterizedFunction<?> parameterizedDimension) {
        Collection<ParameterizedFunction<?>> collection = new ArrayList<>();
        collection.add(parameterizedDimension);
        return collection;
    }

    @Override
    protected GroupKey createGroupKeyFor(DataType input, Function<?> dimension, ParameterProvider parameterProvider) {
        return new GenericGroupKey<FunctionDTO>(dtoFactory.createFunctionDTO(dimension, stringMessages, locale));
    }

}
