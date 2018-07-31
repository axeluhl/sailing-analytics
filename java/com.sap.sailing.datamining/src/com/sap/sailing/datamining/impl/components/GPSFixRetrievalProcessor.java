package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.GPSFixWithContext;
import com.sap.sailing.datamining.impl.data.TrackedLegOfCompetitorWithSpecificTimePointWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class GPSFixRetrievalProcessor extends AbstractRetrievalProcessor<HasTrackedLegOfCompetitorContext, HasGPSFixContext> {

    public GPSFixRetrievalProcessor(ExecutorService executor, Collection<Processor<HasGPSFixContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasTrackedLegOfCompetitorContext.class, HasGPSFixContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasGPSFixContext> retrieveData(HasTrackedLegOfCompetitorContext element) {
        Collection<HasGPSFixContext> gpsFixesWithContext = new ArrayList<>();
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = element.getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getTrack(element.getCompetitor());
        competitorTrack.lockForRead();
        try {
            TrackedLegOfCompetitor trackedLegOfCompetitor = element.getTrackedLegOfCompetitor();
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null) {
                for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                    if (isAborted()) {
                        break;
                    }
                    HasGPSFixContext gpsFixWithContext = new GPSFixWithContext(new TrackedLegOfCompetitorWithSpecificTimePointWithContext(
                            element.getTrackedLegContext(), element.getTrackedLegOfCompetitor(), gpsFix.getTimePoint()), gpsFix);
                    gpsFixesWithContext.add(gpsFixWithContext);
                }
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        return gpsFixesWithContext;
    }

}
