package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.RacingEventService;

public class QueryImpl implements Query {
    private static final long serialVersionUID = 5649156014930954522L;

    private Selector selector;
    private Extractor extractor;
    private Aggregator aggregator;

    public QueryImpl(Selector selector, Extractor extractor, Aggregator aggregator) {
        this.selector = selector;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public Selector getSelector() {
        return selector;
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
    public List<Pair<String, Double>> run(RacingEventService racingEventService) {
        List<Pair<String, Double>> data = new ArrayList<Pair<String, Double>>();
        getSelector().initializeSelection(racingEventService);
        for (String xValue : getSelector().getXValues()) {
            double dataElement = getAggregator().aggregate(getExtractor().extractDataFrom(getSelector().getDataFor(xValue)));
            data.add(new Pair<String, Double>(xValue, dataElement));
        }
        return data;
    }

}
