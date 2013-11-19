package com.sap.sailing.datamining;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.factories.AggregatorFactory;
import com.sap.sailing.datamining.factories.DataRetrieverFactory;
import com.sap.sailing.datamining.factories.ExtractorFactory;
import com.sap.sailing.datamining.factories.FilterFactory;
import com.sap.sailing.datamining.factories.GrouperFactory;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.shared.QueryDefinition;

public final class DataMiningFactory {
    
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    
    private DataMiningFactory() { }

    public static <DataType, AggregatedType extends Number> Query<DataType, AggregatedType> createQuery(QueryDefinition queryDefinition) {
        ParallelDataRetriever<DataType> retriever = DataRetrieverFactory.createDataRetriever(queryDefinition.getDataType(), executor);
        
        ParallelFilter<DataType> filter = createFilter(queryDefinition);
        
        ParallelGrouper<DataType> grouper = GrouperFactory.createGrouper(queryDefinition, executor);
        Extractor<DataType, AggregatedType> extractor = ExtractorFactory.createExtractor(queryDefinition.getStatisticType());
        Aggregator<AggregatedType, AggregatedType> aggregator = AggregatorFactory.createAggregator(queryDefinition.getStatisticType(), queryDefinition.getAggregatorType());
        
        return new QueryImpl<DataType, AggregatedType, AggregatedType>(retriever, filter, grouper, extractor, aggregator);
    }

    private static <DataType> ParallelFilter<DataType> createFilter(QueryDefinition queryDefinition) {
        if (queryDefinition.getSelection().isEmpty()) {
            return FilterFactory.createNoFilter();
        }
        
        WorkerBuilder<FiltrationWorker<DataType>> workerBuilder = FilterFactory.createDimensionFilterBuilder(queryDefinition.getDataType(), queryDefinition.getSelection());
        return FilterFactory.createParallelFilter(workerBuilder, executor);
    }

}
