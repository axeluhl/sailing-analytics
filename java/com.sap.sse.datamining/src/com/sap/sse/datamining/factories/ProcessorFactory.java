package com.sap.sse.datamining.factories;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionalGroupingProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataAverageAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataMedianAggregationProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataSumAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.components.AggregatorType;

public class ProcessorFactory {
    
    private ProcessorFactory() {
    }
    
    public static <AggregatedType, DataSourceType> ProcessorQuery<AggregatedType, DataSourceType> createProcessorQuery(DataSourceType dataSource, String localeInfoName) {
        return new ProcessorQuery<AggregatedType, DataSourceType>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFrom(localeInfoName));
    }
    
    public static Processor<GroupedDataEntry<Double>> createAggregationProcessor(ProcessorQuery<Double, ?> query, AggregatorType aggregatorType) {
        Collection<Processor<Map<GroupKey, Double>>> resultReceivers = Arrays.asList(query.getResultReceiver());
        return createAggregationProcessor(aggregatorType, resultReceivers);
    }
    
    public static Processor<GroupedDataEntry<Double>> createAggregationProcessor(AggregatorType aggregatorType, Collection<Processor<Map<GroupKey, Double>>> resultReceivers) {
        ExecutorService executor = DataMiningActivator.getExecutor();
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

    public static <ElementType> Processor<GroupedDataEntry<ElementType>> createExtractionProcessor(
            Processor<GroupedDataEntry<Double>> aggregationProcessor, Function<Double> extractionFunction) {
        return createExtractionProcessor(Arrays.asList(aggregationProcessor), extractionFunction);
    }

    public static <ElementType> Processor<GroupedDataEntry<ElementType>> createExtractionProcessor(
            Collection<Processor<GroupedDataEntry<Double>>> resultReceivers, Function<Double> extractionFunction) {
        return new ParallelGroupedElementsValueExtractionProcessor<ElementType, Double>(DataMiningActivator.getExecutor(),
                resultReceivers, extractionFunction);
    }
    
    public static <ElementType> Processor<ElementType> createGroupingProcessor(Processor<GroupedDataEntry<ElementType>> extractionProcessor, List<Function<?>> dimensionsToGroupBy) {
        return createGroupingProcessor(Arrays.asList(extractionProcessor), dimensionsToGroupBy);
    }
    
    public static <ElementType> Processor<ElementType> createGroupingProcessor(Collection<Processor<GroupedDataEntry<ElementType>>> resultReceivers, List<Function<?>> dimensionsToGroupBy) {
        return new ParallelMultiDimensionalGroupingProcessor<>(DataMiningActivator.getExecutor(), resultReceivers, dimensionsToGroupBy);
    }

}
