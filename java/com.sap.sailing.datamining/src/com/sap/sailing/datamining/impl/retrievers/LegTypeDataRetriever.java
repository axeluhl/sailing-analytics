package com.sap.sailing.datamining.impl.retrievers;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LegTypeDataRetriever extends TrackedRaceDataRetriever {

    private HashSet<LegType> legTypes;

    public LegTypeDataRetriever(TrackedRace trackedRace, Collection<LegType> legTypes) {
        super(trackedRace);
        this.legTypes = new HashSet<LegType>(legTypes);
    }

    @Override
    protected boolean retrieveDataFor(LegType legType) {
        for (LegType legTypeToCheck : legTypes) {
            if (legTypeToCheck.equals(legType)) {
                return true;
            }
        }
        return false;
    }

}
