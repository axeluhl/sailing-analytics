package com.sap.sailing.datamining.impl.retrievers;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CompetitorSailIDDataRetriever extends TrackedRaceDataRetriever {

    private Collection<String> sailIDs;

    public CompetitorSailIDDataRetriever(TrackedRace trackedRace, Collection<String> sailIDs) {
        super(trackedRace);
        this.sailIDs = new HashSet<String>(sailIDs);
    }

    @Override
    protected boolean retrieveDataFor(Competitor competitor) {
        for (String nationality : sailIDs) {
            if (nationality.equals(competitor.getBoat().getSailID())) {
                return true;
            }
        }
        return false;
    }

}
