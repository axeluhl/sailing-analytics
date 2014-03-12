package com.sap.sailing.datamining.factories;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.factories.AggregatorFactory;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.deprecated.QueryImpl;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class DataMiningFactory {

    private static final String DEFAULT_LOCALE_NAME = "default";
    private static final Map<String, Locale> supportedLocalesMappedByLocaleInfo = new HashMap<>();
    {
        supportedLocalesMappedByLocaleInfo.put(DEFAULT_LOCALE_NAME, Locale.ENGLISH);
        supportedLocalesMappedByLocaleInfo.put("en", Locale.ENGLISH);
        supportedLocalesMappedByLocaleInfo.put("de", Locale.GERMAN);
    }

    private DataMiningFactory() {
    }

    public static <DataType, AggregatedType extends Number> Query<AggregatedType> createQuery(
            QueryDefinition queryDefinition, RacingEventService racingService) {
        Locale locale = getLocaleFrom(queryDefinition.getLocaleInfoName());

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

    private static Locale getLocaleFrom(String localeInfoName) {
        Locale locale = supportedLocalesMappedByLocaleInfo.get(localeInfoName);
        return locale != null ? locale : supportedLocalesMappedByLocaleInfo.get(DEFAULT_LOCALE_NAME);
    }

    private static <DataType> ParallelFilter<DataType> createFilter(QueryDefinition queryDefinition) {
        if (queryDefinition.getSelection().isEmpty()) {
            return FilterFactory.createNonFilteringFilter();
        }

        WorkerBuilder<FiltrationWorker<DataType>> workerBuilder = FilterFactory.createDimensionFilterBuilder(
                queryDefinition.getDataType(), queryDefinition.getSelection());
        return FilterFactory.createParallelFilter(workerBuilder, DataMiningActivator.getExecutor());
    }

}
