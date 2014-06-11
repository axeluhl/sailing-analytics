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
    
    /**
     * Creates a ProcessorQuery with the given data source.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor or the string messages).
     */
    public static <AggregatedType, DataSourceType> ProcessorQuery<AggregatedType, DataSourceType> createProcessorQuery(DataSourceType dataSource, String localeInfoName) {
        return new ProcessorQuery<AggregatedType, DataSourceType>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFrom(localeInfoName));
    }
    
    /**
     * Creates a aggregation processor of the given aggregator type, with the given ProcessorQuery as result receiver.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static Processor<GroupedDataEntry<Double>> createAggregationProcessor(ProcessorQuery<Double, ?> query, AggregatorType aggregatorType) {
        Collection<Processor<Map<GroupKey, Double>>> resultReceivers = Arrays.asList(query.getResultReceiver());
        return createAggregationProcessor(resultReceivers, aggregatorType);
    }

    
    /**
     * Creates an aggregation processor.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static Processor<GroupedDataEntry<Double>> createAggregationProcessor(Collection<Processor<Map<GroupKey, Double>>> resultReceivers, AggregatorType aggregatorType) {
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

    /**
     * Creates an extraction processor with the given aggregation processor as result receiver.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static <ElementType> Processor<GroupedDataEntry<ElementType>> createExtractionProcessor(
            Processor<GroupedDataEntry<Double>> aggregationProcessor, Function<Double> extractionFunction) {
        return createExtractionProcessor(Arrays.asList(aggregationProcessor), extractionFunction);
    }

    /**
     * Creates a extraction processor.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static <ElementType> Processor<GroupedDataEntry<ElementType>> createExtractionProcessor(
            Collection<Processor<GroupedDataEntry<Double>>> resultReceivers, Function<Double> extractionFunction) {
        return new ParallelGroupedElementsValueExtractionProcessor<ElementType, Double>(DataMiningActivator.getExecutor(),
                resultReceivers, extractionFunction);
    }

    /**
     * Creates a grouping processor with the given extraction processor as result receiver.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static <ElementType> Processor<ElementType> createGroupingProcessor(Processor<GroupedDataEntry<ElementType>> extractionProcessor, List<Function<?>> dimensionsToGroupBy) {
        return createGroupingProcessor(Arrays.asList(extractionProcessor), dimensionsToGroupBy);
    }

    /**
     * Creates a grouping processor.<br />
     * The {@link DataMiningActivator} will be used to get the missing objects for the construction (like
     * the executor).
     */
    public static <ElementType> Processor<ElementType> createGroupingProcessor(Collection<Processor<GroupedDataEntry<ElementType>>> resultReceivers, List<Function<?>> dimensionsToGroupBy) {
        return new ParallelMultiDimensionalGroupingProcessor<>(DataMiningActivator.getExecutor(), resultReceivers, dimensionsToGroupBy);
    }

}
