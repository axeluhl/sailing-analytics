package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelByDimensionGroupingProcessor;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionsValueNestingGroupingProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDataCollectingAsSetProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataAverageAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataMedianAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataSumAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class ProcessorFactory {
    
    private final ExecutorService executor;

    public ProcessorFactory(ExecutorService executor) {
        this.executor = executor;
    }
    
    /**
     * Creates a aggregation processor of the given aggregator type, with the given ProcessorQuery as result receiver.
     */
    public <ResultType> Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>> createAggregationProcessor(ProcessorQuery<ResultType, ?> query, AggregatorType aggregatorType, Class<ResultType> resultType) {
        Collection<Processor<Map<GroupKey, ResultType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(query.getResultReceiver());
        return createAggregationProcessor(resultReceivers, aggregatorType, resultType);
    }

    @SuppressWarnings("unchecked")
    public <ResultType> Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>> createAggregationProcessor(Collection<Processor<Map<GroupKey, ResultType>, ?>> resultReceivers, AggregatorType aggregatorType, Class<ResultType> resultType) {
        if (Double.class.equals(resultType) || double.class.equals(resultType)) {
            Collection<Processor<Map<GroupKey, Double>, ?>> specificResultReceivers = (Collection<Processor<Map<GroupKey, Double>, ?>>)(Collection<?>) resultReceivers;

            switch (aggregatorType) {
            case Average:
                return (Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>>)(Processor<?, ?>) new ParallelGroupedDoubleDataAverageAggregationProcessor(executor, specificResultReceivers);
            case Median:
                return (Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>>)(Processor<?, ?>) new ParallelGroupedDoubleDataMedianAggregationProcessor(executor, specificResultReceivers);
            case Sum:
                return (Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>>)(Processor<?, ?>) new ParallelGroupedDoubleDataSumAggregationProcessor(executor, specificResultReceivers);
            default:
                throw new IllegalArgumentException("No aggregation processor implemented for the aggregation type '"
                        + aggregatorType + "'");
            }
        }
        throw new IllegalArgumentException("No aggregation processor implemented for the result type '"
                + resultType + "'");
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
    public <ElementType, ResultType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<ResultType>> createExtractionProcessor(
            Processor<GroupedDataEntry<ResultType>, ?> aggregationProcessor, Function<ResultType> extractionFunction, ParameterProvider parameterProvider) {
        Collection<Processor<GroupedDataEntry<ResultType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(aggregationProcessor);
        return createExtractionProcessor(resultReceivers, extractionFunction, parameterProvider);
    }

    public <ElementType, ResultType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<ResultType>> createExtractionProcessor(
            Collection<Processor<GroupedDataEntry<ResultType>, ?>> resultReceivers, Function<ResultType> extractionFunction, ParameterProvider parameterProvider) {
        return new ParallelGroupedElementsValueExtractionProcessor<ElementType, ResultType>(executor,
                resultReceivers, extractionFunction, parameterProvider);
    }

    /**
     * Creates a grouping processor with the given extraction processor as result receiver.
     */
    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Processor<GroupedDataEntry<DataType>, ?> extractionProcessor,  Iterable<Pair<Function<?>, ParameterProvider>> dimensionsToGroupByWithParameterProvider) {
        Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(extractionProcessor);
        return createGroupingProcessor(dataType, resultReceivers, dimensionsToGroupByWithParameterProvider);
    }

    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers, Iterable<Pair<Function<?>, ParameterProvider>> dimensionsToGroupByWithParameterProvider) {
        return new ParallelMultiDimensionsValueNestingGroupingProcessor<>(dataType, executor, resultReceivers, dimensionsToGroupByWithParameterProvider);
    }

    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessorWithParameter(Class<DataType> dataType, Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers, List<Pair<Function<?>, ParameterProvider>> dimensionsToGroupByWithParameterProvider) {
        return new ParallelMultiDimensionsValueNestingGroupingProcessor<DataType>(dataType, executor, resultReceivers, dimensionsToGroupByWithParameterProvider);
    }

    /**
     * Creates a dimension grouping processor followed by a function value extraction processor for each given dimension.
     */
    @SuppressWarnings("unchecked")
    public <DataType> Collection<Processor<DataType, GroupedDataEntry<DataType>>> createGroupingExtractorsForDimensions(Class<DataType> dataType,
            Processor<GroupedDataEntry<Object>, ?> valueCollector, Iterable<Pair<Function<?>, ParameterProvider>> dimensionsToGroupByWithParameterProvider,
            ResourceBundleStringMessages stringMessages, Locale locale) {
        Collection<Processor<GroupedDataEntry<Object>, ?>> extractionResultReceivers = new ArrayList<>();
        extractionResultReceivers.add(valueCollector);

        Collection<Processor<DataType, GroupedDataEntry<DataType>>> groupingExtractors = new ArrayList<>();
        for (Pair<Function<?>, ParameterProvider> dimensionWithParameterProvider : dimensionsToGroupByWithParameterProvider) {
            Processor<GroupedDataEntry<DataType>, GroupedDataEntry<Object>> dimensionValueExtractor = createExtractionProcessor(extractionResultReceivers, (Function<Object>) dimensionWithParameterProvider.getA(), dimensionWithParameterProvider.getB());
            Collection<Processor<GroupedDataEntry<DataType>, ?>> groupingResultReceivers = new ArrayList<>();
            groupingResultReceivers.add(dimensionValueExtractor);
            
            Processor<DataType, GroupedDataEntry<DataType>> byDimensionGrouper = new ParallelByDimensionGroupingProcessor<>(dataType, executor, groupingResultReceivers, dimensionWithParameterProvider.getA(), stringMessages, locale);
            groupingExtractors.add(byDimensionGrouper);
        }
        return groupingExtractors;
    }

}
