package com.sap.sailing.datamining.impl.retrievers;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class CompetitorDetailDataRetriever extends AbstractTrackedRaceDataRetriever {

    public CompetitorDetailDataRetriever(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            if (retrieveDataFor(competitor)) {
                data.addAll(trackToGPSFixesWithContext(getTrackedRace().getTrack(competitor)));
            }
        }
        return data;
    }

    protected abstract boolean retrieveDataFor(Competitor competitor);

}
