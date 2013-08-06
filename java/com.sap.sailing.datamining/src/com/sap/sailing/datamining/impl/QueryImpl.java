package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.QueryResultImpl;
import com.sap.sailing.server.RacingEventService;

public class QueryImpl implements Query {

    private Selector selector;
    private Grouper grouper;

    private Extractor extractor;
    private Aggregator aggregator;
    
    public QueryImpl(Selector selector, Grouper grouper, Extractor extractor, Aggregator aggregator) {
        this.selector = selector;
        this.grouper = grouper;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public Grouper getGrouper() {
        return grouper;
    }

    @Override
    public Extractor getExtractor() {
        return extractor;
    }

    @Override
    public Aggregator getAggregator() {
        return aggregator;
    }

    @Override
    public QueryResult run(RacingEventService racingEventService) {
        List<GPSFixWithContext> selectedFixes = getSelector().selectGPSFixes(racingEventService);
        QueryResultImpl result = new QueryResultImpl(selectedFixes.size());
        Map<String, Collection<GPSFixWithContext>> groupedFixes = getGrouper().group(selectedFixes);
        for (Entry<String, Collection<GPSFixWithContext>> groupEntry : groupedFixes.entrySet()) {
            Collection<Double> extractedData = extractor.extract(groupEntry.getValue());
            Double aggregatedData = aggregator.aggregate(extractedData);
            result.addResult(groupEntry.getKey(), aggregatedData);
        }
        return result;
    }

}
