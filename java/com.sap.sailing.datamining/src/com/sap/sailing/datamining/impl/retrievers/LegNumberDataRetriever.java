package com.sap.sailing.datamining.impl.retrievers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LegNumberDataRetriever extends AbstractTrackedRaceDataRetriever {

    private Collection<Integer> legNumbers;

    public LegNumberDataRetriever(TrackedRace trackedRace, Collection<Integer> legNumbers) {
        super(trackedRace);
        this.legNumbers = new HashSet<Integer>(legNumbers);
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        int legIndex = 1;
        for (Leg leg : getTrackedRace().getRace().getCourse().getLegs()) {
            for (Integer legNumber : legNumbers) {
                if (legNumber.equals(legIndex)) {
                    data.addAll(legToGPSFixesWithContext(leg));
                }
            }
            legIndex++;
        }
        return data;
    }

}
