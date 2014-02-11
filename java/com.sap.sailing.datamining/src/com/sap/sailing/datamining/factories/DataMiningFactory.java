package com.sap.sailing.datamining.factories;

import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.i18n.DataMiningResourceBundleManager;
import com.sap.sailing.datamining.impl.Activator;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class DataMiningFactory {
    
    private static final DataMiningStringMessages stringMessages = new DataMiningResourceBundleManager(Locale.ENGLISH);
    
    private DataMiningFactory() { }

    public static <DataType, AggregatedType extends Number> Query<DataType, AggregatedType> createQuery(QueryDefinition queryDefinition, RacingEventService racingService) {
        Locale locale = stringMessages.getLocaleFrom(queryDefinition.getLocaleName());
        
        ParallelDataRetriever<DataType> retriever = DataRetrieverFactory.createDataRetriever(queryDefinition.getDataType(), racingService, getExecutor());
        
        ParallelFilter<DataType> filter = createFilter(queryDefinition);
        
        ParallelGrouper<DataType> grouper = GrouperFactory.createGrouper(queryDefinition, getExecutor());
        ParallelExtractor<DataType, AggregatedType> extractor = ExtractorFactory.createExtractor(stringMessages, locale, queryDefinition.getStatisticType(), getExecutor());
        ParallelAggregator<AggregatedType, AggregatedType> aggregator = AggregatorFactory.createAggregator(stringMessages, locale, queryDefinition.getStatisticType().getValueType(),
                                                                                                           queryDefinition.getAggregatorType(), getExecutor());
        
        return new QueryImpl<DataType, AggregatedType, AggregatedType>(stringMessages, locale, retriever, filter, grouper, extractor, aggregator);
    }

    private static <DataType> ParallelFilter<DataType> createFilter(QueryDefinition queryDefinition) {
        if (queryDefinition.getSelection().isEmpty()) {
            return FilterFactory.createNonFilteringFilter();
        }
        
        WorkerBuilder<FiltrationWorker<DataType>> workerBuilder = FilterFactory.createDimensionFilterBuilder(queryDefinition.getDataType(), queryDefinition.getSelection());
        return FilterFactory.createParallelFilter(workerBuilder, getExecutor());
    }
    
    public static ThreadPoolExecutor getExecutor() {
        return Activator.getExecutor();
    }

}
