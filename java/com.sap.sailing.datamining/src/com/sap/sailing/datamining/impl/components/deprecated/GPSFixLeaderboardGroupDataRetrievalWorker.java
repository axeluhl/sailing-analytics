package com.sap.sailing.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.HasGPSFixContextImpl;
import com.sap.sailing.datamining.impl.gps_fix.GPSFixWithContextImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class GPSFixLeaderboardGroupDataRetrievalWorker extends AbstractLeaderboardGroupDataRetrievalWorker<GPSFixWithContext> {
    
    @Override
    public Collection<GPSFixWithContext> doWork() {
        Collection<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        Collection<Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>> baseData = retrieveDataTillTrackedLegOfCompetitor(getGroup());
        for (Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext> baseDataEntry : baseData) {
            HasGPSFixContext context = new HasGPSFixContextImpl(baseDataEntry.getB());
            data.addAll(retrieveDataFor(context));
        }
        return data;
    }

    private Collection<GPSFixWithContext> retrieveDataFor(HasGPSFixContext context) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        TrackedLegOfCompetitor trackedLegOfCompetitor = context.getTrackedLeg().getTrackedLeg(context.getCompetitor());
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = context.getTrackedRace().getTrack(context.getCompetitor());
        competitorTrack.lockForRead();
        try {
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null) {
                for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                    data.add(new GPSFixWithContextImpl(gpsFix, context));
                }
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        return data;
    }

}
