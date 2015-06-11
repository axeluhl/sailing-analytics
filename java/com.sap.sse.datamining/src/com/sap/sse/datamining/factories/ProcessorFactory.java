package com.sap.sse.datamining.factories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelByDimensionGroupingProcessor;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionsValueNestingGroupingProcessor;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDataCollectingAsSetProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class ProcessorFactory {
    
    private final ExecutorService executor;

    public ProcessorFactory(ExecutorService executor) {
        this.executor = executor;
    }
    
    public <ExtractedType, AggregatedType> Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, AggregatedType>>
                                           createAggregationProcessor(ProcessorQuery<AggregatedType, ?> query,
                                                                      AggregationProcessorDefinition<ExtractedType, AggregatedType> aggregatorDefinition) {
        Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(query.getResultReceiver());
        return createAggregationProcessor(resultReceivers, aggregatorDefinition);
    }
    
    public <ExtractedType, AggregatedType> Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, AggregatedType>>
                                           createAggregationProcessor(Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers,
                                                                      AggregationProcessorDefinition<ExtractedType, AggregatedType> aggregatorDefinition) {
        Class<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> aggregatorType = aggregatorDefinition.getAggregationProcessor();
        Constructor<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> aggregatorConstructor = null;
        try {
            aggregatorConstructor = aggregatorType.getConstructor(ExecutorService.class, Collection.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given aggregatorDefinition '"
                    + aggregatorDefinition + "'", e);
        }

        if (Modifier.isPublic(aggregatorConstructor.getModifiers())) {
            // Preventing IllegalAccessExceptions of public constructors due to weird package behaviour
            aggregatorConstructor.setAccessible(true);
        }
        try {
            return aggregatorConstructor.newInstance(executor, resultReceivers);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Couldn't create a aggregator instance with the constructor "
                    + aggregatorConstructor.toString(), e);
        }
    }
    
    /**
     * Creates a collecting processor with the given ProcessorQuery as result receiver, that stores all received grouped
     * data in a Set.
     */
    public Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> createGroupedDataCollectingAsSetProcessor(ProcessorQuery<Set<Object>, ?> query) {
        Collection<Processor<Map<GroupKey, Set<Object>>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(query.getResultReceiver());
        return new ParallelGroupedDataCollectingAsSetProcessor<>(executor, resultReceivers);
    }

    /**
     * Creates an extraction processor with the given aggregation processor as result receiver.
     */
    public <ElementType, ExtractedType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<ExtractedType>> createExtractionProcessor(
            Processor<GroupedDataEntry<ExtractedType>, ?> aggregationProcessor, Function<ExtractedType> extractionFunction, ParameterProvider parameterProvider) {
        Collection<Processor<GroupedDataEntry<ExtractedType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(aggregationProcessor);
        return createExtractionProcessor(resultReceivers, extractionFunction, parameterProvider);
    }

    public <ElementType, ExtractedType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<ExtractedType>> createExtractionProcessor(
            Collection<Processor<GroupedDataEntry<ExtractedType>, ?>> resultReceivers, Function<ExtractedType> extractionFunction, ParameterProvider parameterProvider) {
        return new ParallelGroupedElementsValueExtractionProcessor<ElementType, ExtractedType>(executor,
                resultReceivers, extractionFunction, parameterProvider);
    }

    /**
     * Creates a grouping processor with the given extraction processor as result receiver.
     */
    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Processor<GroupedDataEntry<DataType>, ?> extractionProcessor,
            Iterable<ParameterizedFunction<?>> parameterizedDimensions) {
        Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(extractionProcessor);
        return createGroupingProcessor(dataType, resultReceivers, parameterizedDimensions);
    }

    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
            Iterable<ParameterizedFunction<?>> parameterizedDimensions) {
        return new ParallelMultiDimensionsValueNestingGroupingProcessor<>(dataType, executor, resultReceivers, parameterizedDimensions);
    }

    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessorWithParameter(Class<DataType> dataType, Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers,
            List<ParameterizedFunction<?>> parameterizedDimensions) {
        return new ParallelMultiDimensionsValueNestingGroupingProcessor<DataType>(dataType, executor, resultReceivers, parameterizedDimensions);
    }

    /**
     * Creates a dimension grouping processor followed by a function value extraction processor for each given dimension.
     */
    @SuppressWarnings("unchecked")
    public <DataType> Collection<Processor<DataType, GroupedDataEntry<DataType>>> createGroupingExtractorsForDimensions(Class<DataType> dataType,
            Processor<GroupedDataEntry<Object>, ?> valueCollector, Iterable<ParameterizedFunction<?>> parameterizedDimensions,
            ResourceBundleStringMessages stringMessages, Locale locale) {
        Collection<Processor<GroupedDataEntry<Object>, ?>> extractionResultReceivers = new ArrayList<>();
        extractionResultReceivers.add(valueCollector);

        Collection<Processor<DataType, GroupedDataEntry<DataType>>> groupingExtractors = new ArrayList<>();
        for (ParameterizedFunction<?> parameterizedDimension : parameterizedDimensions) {
            Processor<GroupedDataEntry<DataType>, GroupedDataEntry<Object>> dimensionValueExtractor = createExtractionProcessor(extractionResultReceivers, (Function<Object>) parameterizedDimension.getFunction(), parameterizedDimension.getParameterProvider());
            Collection<Processor<GroupedDataEntry<DataType>, ?>> groupingResultReceivers = new ArrayList<>();
            groupingResultReceivers.add(dimensionValueExtractor);
            
            Processor<DataType, GroupedDataEntry<DataType>> byDimensionGrouper = new ParallelByDimensionGroupingProcessor<>(dataType, executor, groupingResultReceivers, parameterizedDimension, stringMessages, locale);
            groupingExtractors.add(byDimensionGrouper);
        }
        return groupingExtractors;
    }

}
