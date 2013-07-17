package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.common.impl.Util.Pair;
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
    public List<Pair<String, Double>> run(RacingEventService racingEventService) {
        List<Pair<String, Double>> data = new ArrayList<Pair<String, Double>>();
        List<GPSFixWithContext> selectedFixes = selector.selectGPSFixes(racingEventService);
        data.add(new Pair<String, Double>("Number of selected and retrieved fixes", new Double(selectedFixes.size())));
        return data;
    }

}
