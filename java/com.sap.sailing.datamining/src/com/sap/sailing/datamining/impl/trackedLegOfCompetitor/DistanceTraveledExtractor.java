package com.sap.sailing.datamining.impl.trackedLegOfCompetitor;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractExtractor;
import com.sap.sailing.datamining.shared.Unit;
import com.sap.sailing.domain.common.Distance;

public class DistanceTraveledExtractor extends AbstractExtractor<TrackedLegOfCompetitorWithContext, Double> {

    public DistanceTraveledExtractor() {
        super("distance in meters", Unit.Meters, 2);
    }

    @Override
    public Double extract(TrackedLegOfCompetitorWithContext dataEntry) {
        Distance distanceTraveled = dataEntry.getDistanceTraveled();
        return distanceTraveled != null ? distanceTraveled.getMeters() : 0;
    }

}
