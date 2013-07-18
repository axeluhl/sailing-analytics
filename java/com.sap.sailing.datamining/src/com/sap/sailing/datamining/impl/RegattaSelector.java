package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;

public class RegattaSelector implements Selector {
    
    private String[] regattaNames;

    public RegattaSelector(String... regattaNames) {
        this.regattaNames = regattaNames;
    }

    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        Iterable<Regatta> regattas = racingEventService.getAllRegattas();
        for (String regattaName : regattaNames) {
            for (Regatta regatta : regattas) {
                if (regattaName.equals(regatta.getName())) {
                    TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
                    if (trackedRegatta != null) {
                        data.addAll(new TrackedRegattaDataRetriever(trackedRegatta).retrieveData());
                    }
                }
            }
        }
        return data;
    }

}
