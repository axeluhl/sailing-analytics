package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.RunnableQuery;
import com.sap.sailing.datamining.shared.Aggregator;
import com.sap.sailing.datamining.shared.Extractor;
import com.sap.sailing.datamining.shared.Query;
import com.sap.sailing.datamining.shared.Selector;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.RacingEventService;

public class RunnableQueryImpl implements RunnableQuery {
    private static final long serialVersionUID = -6602062328803548281L;
    
    private Query query;

    public RunnableQueryImpl(Query query) {
        this.query = query;
    }

    @Override
    public Selector getSelector() {
        return query.getSelector();
    }

    @Override
    public Extractor getExtractor() {
        return query.getExtractor();
    }

    @Override
    public Aggregator getAggregator() {
        return query.getAggregator();
    }

    @Override
    public List<Pair<String, Double>> run(RacingEventService racingEventService) {
        List<Pair<String, Double>> data = new ArrayList<>();
        ((AbstractSelector) getSelector()).initializeSelection(racingEventService);
        for (String xValue : getSelector().getXValues()) {
            double dataElement = getAggregator().aggregate(getExtractor().extractDataFrom(getSelector().getDataFor(xValue)));
            data.add(new Pair<String, Double>(xValue, dataElement));
        }
        return data;
    }

}
