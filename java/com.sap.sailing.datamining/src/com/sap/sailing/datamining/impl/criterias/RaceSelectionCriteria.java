package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.impl.retrievers.TrackedRaceDataRetriever;

public class RaceSelectionCriteria extends AbstractSelectionCriteria<String> {

    public RaceSelectionCriteria(Collection<String> raceNames) {
        super(raceNames);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRace() == null) {
            return false;
        }
        
        for (String raceName : getSelection()) {
            if (raceName.equals(context.getTrackedRace().getRace().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        return new TrackedRaceDataRetriever(context.getTrackedRace());
    }

}
