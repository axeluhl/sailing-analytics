package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
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

public class ProcessorFactory {
    
    private final ExecutorService executor;

    public ProcessorFactory(ExecutorService executor) {
        this.executor = executor;
    }
    
    /**
     * Creates a aggregation processor of the given aggregator type, with the given ProcessorQuery as result receiver.
     */
    public Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> createAggregationProcessor(ProcessorQuery<Double, ?> query, AggregatorType aggregatorType) {
        Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(query.getResultReceiver());
        return createAggregationProcessor(resultReceivers, aggregatorType);
    }

    public Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> createAggregationProcessor(Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers, AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new ParallelGroupedDoubleDataAverageAggregationProcessor(executor, resultReceivers);
        case Median:
            return new ParallelGroupedDoubleDataMedianAggregationProcessor(executor, resultReceivers);
        case Sum:
            return new ParallelGroupedDoubleDataSumAggregationProcessor(executor, resultReceivers);
        default:
            throw new IllegalArgumentException("No aggregation processor implemented for the aggregation type '" + aggregatorType + "'");
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
    public <ElementType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<Double>> createExtractionProcessor(
            Processor<GroupedDataEntry<Double>, ?> aggregationProcessor, Function<Double> extractionFunction) {
        Collection<Processor<GroupedDataEntry<Double>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(aggregationProcessor);
        return createExtractionProcessor(resultReceivers, extractionFunction);
    }

    public <ElementType> Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<Double>> createExtractionProcessor(
            Collection<Processor<GroupedDataEntry<Double>, ?>> resultReceivers, Function<Double> extractionFunction) {
        return new ParallelGroupedElementsValueExtractionProcessor<ElementType, Double>(executor,
                resultReceivers, extractionFunction);
    }

    /**
     * Creates a grouping processor with the given extraction processor as result receiver.
     */
    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Processor<GroupedDataEntry<DataType>, ?> extractionProcessor,  List<Function<?>> dimensionsToGroupBy) {
        Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(extractionProcessor);
        return createGroupingProcessor(dataType, resultReceivers, dimensionsToGroupBy);
    }

    public <DataType> Processor<DataType, GroupedDataEntry<DataType>> createGroupingProcessor(Class<DataType> dataType, Collection<Processor<GroupedDataEntry<DataType>, ?>> resultReceivers, List<Function<?>> dimensionsToGroupBy) {
        return new ParallelMultiDimensionsValueNestingGroupingProcessor<>(dataType, executor, resultReceivers, dimensionsToGroupBy);
    }

    /**
     * Creates a dimension grouping processor followed by a function value extraction processor for each given dimension.
     */
    @SuppressWarnings("unchecked")
    public <DataType> Collection<Processor<DataType, GroupedDataEntry<DataType>>> createGroupingExtractorsForDimensions(Class<DataType> dataType,
            Processor<GroupedDataEntry<Object>, ?> valueCollector, Collection<Function<?>> dimensions,
            DataMiningStringMessages stringMessages, Locale locale) {
        Collection<Processor<GroupedDataEntry<Object>, ?>> extractionResultReceivers = new ArrayList<>();
        extractionResultReceivers.add(valueCollector);

        Collection<Processor<DataType, GroupedDataEntry<DataType>>> groupingExtractors = new ArrayList<>();
        for (Function<?> dimension : dimensions) {
            Processor<GroupedDataEntry<DataType>, GroupedDataEntry<Object>> dimensionValueExtractor = new ParallelGroupedElementsValueExtractionProcessor<DataType, Object>(executor, extractionResultReceivers, (Function<Object>) dimension);
            Collection<Processor<GroupedDataEntry<DataType>, ?>> groupingResultReceivers = new ArrayList<>();
            groupingResultReceivers.add(dimensionValueExtractor);
            
            Processor<DataType, GroupedDataEntry<DataType>> byDimensionGrouper = new ParallelByDimensionGroupingProcessor<>(dataType, executor, groupingResultReceivers, dimension, stringMessages, locale);
            groupingExtractors.add(byDimensionGrouper);
        }
        return groupingExtractors;
    }

}
