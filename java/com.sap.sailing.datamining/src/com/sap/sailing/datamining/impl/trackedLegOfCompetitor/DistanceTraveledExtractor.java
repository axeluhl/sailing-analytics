package com.sap.sailing.datamining.impl.trackedLegOfCompetitor;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractExtractionWorker;
import com.sap.sailing.domain.common.Distance;

public class DistanceTraveledExtractor extends AbstractExtractionWorker<TrackedLegOfCompetitorWithContext, Double> {

    @Override
    public Double extract(TrackedLegOfCompetitorWithContext dataEntry) {
        Distance distanceTraveled = dataEntry.getDistanceTraveled();
        return distanceTraveled != null ? distanceTraveled.getMeters() : 0;
    }

}
