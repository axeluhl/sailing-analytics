package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.shared.Message;
import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;

public class QueryImpl<DataType, ExtractedType, AggregatedType> implements Query<DataType, AggregatedType> {

    private DataMiningStringMessages stringMessages;
    private Locale locale;

    private ParallelDataRetriever<DataType> retriever;
    private ParallelFilter<DataType> filter;
    private ParallelGrouper<DataType> grouper;

    private ParallelExtractor<DataType, ExtractedType> extractor;
    private ParallelAggregator<ExtractedType, AggregatedType> aggregator;
    
    public QueryImpl(DataMiningStringMessages stringMessages, Locale locale, ParallelDataRetriever<DataType> retriever, ParallelFilter<DataType> filter,
                     ParallelGrouper<DataType> grouper, ParallelExtractor<DataType, ExtractedType> extractor, ParallelAggregator<ExtractedType, AggregatedType> aggregator) {
        this.stringMessages = stringMessages;
        this.locale = locale;
        this.retriever = retriever;
        this.filter = filter;
        this.grouper = grouper;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public QueryResult<AggregatedType> run() throws InterruptedException, ExecutionException {
        final long startTime = System.nanoTime();
        
        Collection<DataType> retrievedData = retriever.start(null).get();
        Collection<DataType> filteredData = filter.start(retrievedData).get();
        Map<GroupKey, Collection<DataType>> groupedData = grouper.start(filteredData).get();
        Map<GroupKey, Collection<ExtractedType>> extractedData = extractor.start(groupedData).get();
        Map<GroupKey, AggregatedType> aggregatedData = aggregator.start(extractedData).get();

        QueryResultImpl<AggregatedType> result = new QueryResultImpl<AggregatedType>(retrievedData.size(), filteredData.size(), createResultSignifier(), extractor.getUnit(), extractor.getValueDecimals());
        for (Entry<GroupKey, AggregatedType> resultEntry : aggregatedData.entrySet()) {
            result.addResult(resultEntry.getKey(), resultEntry.getValue());
        }
        
        final long endTime = System.nanoTime();
        result.setCalculationTimeInNanos(endTime - startTime);
        return result;
    }

    private String createResultSignifier() {
        return stringMessages.get(locale, Message.ResultSignifier, extractor.getSignifier(), aggregator.getName());
    }

}
