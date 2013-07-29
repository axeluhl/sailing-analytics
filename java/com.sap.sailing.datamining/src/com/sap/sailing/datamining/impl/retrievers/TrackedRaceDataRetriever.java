package com.sap.sailing.datamining.impl.retrievers;


import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceDataRetriever extends AbstractTrackedRaceDataRetriever {

    public TrackedRaceDataRetriever(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    protected boolean retrieveDataFor(Competitor competitor) {
        return true;
    }

}
