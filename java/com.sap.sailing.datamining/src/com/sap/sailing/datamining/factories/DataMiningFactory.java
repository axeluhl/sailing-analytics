package com.sap.sailing.datamining.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sap.sailing.datamining.impl.DeprecatedToFunctionConverter;
import com.sap.sailing.datamining.impl.QueryDefinitionConverter;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.AggregatorFactory;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.deprecated.QueryImpl;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class DataMiningFactory {

    private DataMiningFactory() {
    }
    
    // TODO Delete, after the deprecated components have been removed
    public <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinitionDeprecated queryDefinition, DataSourceType dataSource) {
        return createQuery(QueryDefinitionConverter.convertDeprecatedQueryDefinition(queryDefinition), dataSource);
    }

    // TODO Make public, after the deprecated components have been removed
    private static <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinition queryDefinition, DataSourceType dataSource) {
        ProcessorQuery<Double, DataSourceType> query = ProcessorFactory.createProcessorQuery(dataSource, queryDefinition.getLocaleInfoName());
        Processor<GroupedDataEntry<Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(query, queryDefinition.getAggregatorType());
        
        Function<Double> extractionFunction = getExtractionFunction(queryDefinition);
        Processor<GroupedDataEntry<ElementType>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
        
        List<Function<?>> dimensionsToGroupBy = getDimensionsToGroupBy(queryDefinition);
        Processor<ElementType> groupingProcessor = ProcessorFactory.createGroupingProcessor(extractionProcessor, dimensionsToGroupBy);
        
        Processor<DataSourceType> firstRetrievalProcessor = DataRetrieverFactory.createRetrievalProcessorChain(groupingProcessor, queryDefinition.getFilterSelection());
        
        query.setFirstProcessor(firstRetrievalProcessor);
        return query;
    }

    //TODO Get the function from the function provider, not the converter
    @SuppressWarnings("unchecked") // All extraction functions in the DeprecatedToFunctionConverter have the return type Double
    private static Function<Double> getExtractionFunction(QueryDefinition queryDefinition) {
        return (Function<Double>) DeprecatedToFunctionConverter.getFunctionFor(queryDefinition.getExtractionFunction());
    }

    //TODO Get the functions from the function provider, not the converter
    private static List<Function<?>> getDimensionsToGroupBy(QueryDefinition queryDefinition) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupBy.add(DeprecatedToFunctionConverter.getFunctionFor(functionDTO));
        }
        return dimensionsToGroupBy;
    }

    @Deprecated
    public static <DataType, AggregatedType extends Number> Query<AggregatedType> createQuery(
            QueryDefinitionDeprecated queryDefinition, RacingEventService racingService) {
        Locale locale = DataMiningStringMessages.Util.getLocaleFrom(queryDefinition.getLocaleInfoName());

        ParallelDataRetriever<DataType> retriever = DataRetrieverFactory.createDataRetriever(
                queryDefinition.getDataType(), racingService, DataMiningActivator.getExecutor());

        ParallelFilter<DataType> filter = createFilter(queryDefinition);

        ParallelGrouper<DataType> grouper = GrouperFactory.createGrouper(queryDefinition,
                DataMiningActivator.getExecutor());
        ParallelExtractor<DataType, AggregatedType> extractor = ExtractorFactory.createExtractor(
                DataMiningActivator.getStringMessages(), locale, queryDefinition.getStatisticType(),
                DataMiningActivator.getExecutor());
        ParallelAggregator<AggregatedType, AggregatedType> aggregator = AggregatorFactory.createAggregator(
                DataMiningActivator.getStringMessages(), locale, queryDefinition.getStatisticType().getValueType(),
                queryDefinition.getAggregatorType(), DataMiningActivator.getExecutor());

        return new QueryImpl<DataType, AggregatedType, AggregatedType>(DataMiningActivator.getStringMessages(), locale,
                retriever, filter, grouper, extractor, aggregator);
    }

    private static <DataType> ParallelFilter<DataType> createFilter(QueryDefinitionDeprecated queryDefinition) {
        if (queryDefinition.getSelection().isEmpty()) {
            return FilterFactory.createNonFilteringFilter();
        }

        WorkerBuilder<FiltrationWorker<DataType>> workerBuilder = FilterFactory.createDimensionFilterBuilder(
                queryDefinition.getDataType(), queryDefinition.getSelection());
        return FilterFactory.createParallelFilter(workerBuilder, DataMiningActivator.getExecutor());
    }

}
