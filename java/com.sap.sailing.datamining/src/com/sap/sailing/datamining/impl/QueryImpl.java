package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.ParallelDataRetriever;
import com.sap.sailing.datamining.ParallelFilter;
import com.sap.sailing.datamining.ParallelGrouper;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.QueryResultImpl;
import com.sap.sailing.server.RacingEventService;

public class QueryImpl<DataType, ExtractedType, AggregatedType> implements Query<DataType, AggregatedType> {

    private ParallelDataRetriever<DataType> retriever;
    private ParallelFilter<DataType> filter;
    private ParallelGrouper<DataType> grouper;

    private Extractor<DataType, ExtractedType> extractor;
    private Aggregator<ExtractedType, AggregatedType> aggregator;
    
    public QueryImpl(ParallelDataRetriever<DataType> retriever, ParallelFilter<DataType> filter, ParallelGrouper<DataType> grouper, Extractor<DataType, ExtractedType> extractor, Aggregator<ExtractedType, AggregatedType> aggregator) {
        this.retriever = retriever;
        this.filter = filter;
        this.grouper = grouper;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public QueryResult<AggregatedType> run(RacingEventService racingEventService) throws InterruptedException, ExecutionException {
        final long startTime = System.nanoTime();
        
        Collection<DataType> retrievedData = retriever.start(racingEventService).get();
        Collection<DataType> filteredData = filter.start(retrievedData).get();
        QueryResultImpl<AggregatedType> result = new QueryResultImpl<AggregatedType>(retrievedData.size(), filteredData.size(), createResultSignifier(), extractor.getUnit(), extractor.getValueDecimals());
        Map<GroupKey, Collection<DataType>> groupedFixes = grouper.start(filteredData).get();
        for (Entry<GroupKey, Collection<DataType>> groupEntry : groupedFixes.entrySet()) {
            Collection<ExtractedType> extractedData = extractor.extract(groupEntry.getValue());
            AggregatedType aggregatedData = aggregator.aggregate(extractedData);
            result.addResult(groupEntry.getKey(), aggregatedData);
        }
        
        final long endTime = System.nanoTime();
        result.setCalculationTimeInNanos(endTime - startTime);
        return result;
    }

    private String createResultSignifier() {
        return aggregator.getName() + " of the " + extractor.getSignifier();
    }

}
