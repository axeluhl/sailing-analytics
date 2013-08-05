package com.sap.sailing.datamining.impl.retrievers;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.domain.tracking.TrackedRace;

public class LegNumberDataRetriever extends TrackedRaceDataRetriever {

    private Collection<Integer> legNumbers;

    public LegNumberDataRetriever(TrackedRace trackedRace, Collection<Integer> legNumbers) {
        super(trackedRace);
        this.legNumbers = new HashSet<Integer>(legNumbers);
    }

    @Override
    protected boolean retrieveDataFor(int legNumber) {
        for (Integer legNumberToCheck : legNumbers) {
            if (legNumberToCheck.equals(legNumber)) {
                return true;
            }
        }
        return false;
    }

}
