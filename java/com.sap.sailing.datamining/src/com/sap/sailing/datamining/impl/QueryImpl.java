package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.QueryResultImpl;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.server.RacingEventService;

public class QueryImpl<DataType, ValueType, AveragesTo> implements Query<DataType, ValueType, AveragesTo> {

    private DataRetriever<DataType> retriever;
    private Filter<DataType> filter;
    private Grouper<DataType> grouper;

    private Extractor<DataType, ValueType, AveragesTo> extractor;
    private Aggregator<ValueType, AveragesTo> aggregator;
    
    public QueryImpl(DataRetriever<DataType> retriever, Filter<DataType> filter, Grouper<DataType> grouper, Extractor<DataType, ValueType, AveragesTo> extractor, Aggregator<ValueType, AveragesTo> aggregator) {
        this.retriever = retriever;
        this.filter = filter;
        this.grouper = grouper;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public QueryResult<ValueType, AveragesTo> run(RacingEventService racingEventService) {
        Collection<DataType> filteredData = filter.filter(retriever.retrieveData(racingEventService));
        QueryResultImpl<ValueType, AveragesTo> result = new QueryResultImpl<ValueType, AveragesTo>(filteredData.size());
        Map<GroupKey, Collection<DataType>> groupedFixes = grouper.group(filteredData);
        for (Entry<GroupKey, Collection<DataType>> groupEntry : groupedFixes.entrySet()) {
            Collection<ScalableValue<ValueType,AveragesTo>> extractedData = extractor.extract(groupEntry.getValue());
            ScalableValue<ValueType, AveragesTo> aggregatedData = aggregator.aggregate(extractedData);
            result.addResult(groupEntry.getKey(), aggregatedData);
        }
        return result;
    }

}
