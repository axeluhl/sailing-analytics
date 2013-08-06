package com.sap.sailing.datamining.impl;

import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.QueryResultImpl;
import com.sap.sailing.server.RacingEventService;

public class QueryImpl implements Query {

    private Selector selector;

    public QueryImpl(Selector selector) {
        this.selector = selector;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public QueryResult run(RacingEventService racingEventService) {
        List<GPSFixWithContext> selectedFixes = getSelector().selectGPSFixes(racingEventService);
        QueryResultImpl result = new QueryResultImpl(selectedFixes.size());
        return result;
    }

}
