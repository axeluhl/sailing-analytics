package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class TrackedRegattaDataRetriever implements DataRetriever {

    private TrackedRegatta regatta;

    public TrackedRegattaDataRetriever(TrackedRegatta regatta) {
        this.regatta = regatta;
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (TrackedRace trackedRace : regatta.getTrackedRaces()) {
            data.addAll(new TrackedRaceDataRetriever(trackedRace).retrieveData());
        }
        return data;
    }

}
