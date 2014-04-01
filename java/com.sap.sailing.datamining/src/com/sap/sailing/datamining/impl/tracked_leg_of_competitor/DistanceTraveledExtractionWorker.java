package com.sap.sailing.datamining.impl.tracked_leg_of_competitor;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.domain.common.Distance;
import com.sap.sse.datamining.impl.workers.extractors.AbstractExtractionWorker;

public class DistanceTraveledExtractionWorker extends AbstractExtractionWorker<TrackedLegOfCompetitorWithContext, Double> {

    @Override
    public Double extract(TrackedLegOfCompetitorWithContext dataEntry) {
        Distance distanceTraveled = dataEntry.getDistanceTraveled();
        return distanceTraveled != null ? distanceTraveled.getMeters() : 0;
    }

}
