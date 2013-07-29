package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;

public class SelectorImpl implements Selector {
    
    private SelectionCriteria criteria;

    public SelectorImpl(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Regatta regatta : racingEventService.getAllRegattas()) {
            TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                SelectionContext context = new SelectionContextImpl(trackedRegatta, null);
                if (criteria.matches(context)) {
                    data.addAll(criteria.getDataRetriever(context).retrieveData());
                    continue;
                }

                for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                    context = new SelectionContextImpl(trackedRegatta, trackedRace);
                    if (criteria.matches(context)) {
                        data.addAll(criteria.getDataRetriever(context).retrieveData());
                        continue;
                    }
                }
            }
        }
        return data;
    }

}
