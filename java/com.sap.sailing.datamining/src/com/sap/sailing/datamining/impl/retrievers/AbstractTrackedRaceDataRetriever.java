package com.sap.sailing.datamining.impl.retrievers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.GPSFixContextImpl;
import com.sap.sailing.datamining.impl.GPSFixWithContextImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractTrackedRaceDataRetriever implements DataRetriever {

    private TrackedRace trackedRace;

    public AbstractTrackedRaceDataRetriever(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    protected TrackedRace getTrackedRace() {
        return trackedRace;
    }
    
    protected List<GPSFixWithContext> trackToGPSFixesWithContext(GPSFixTrack<Competitor, GPSFixMoving> track) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        track.lockForRead();
        try {
            for (GPSFixMoving gpsFix : track.getFixes()) {
                GPSFixContext context = new GPSFixContextImpl(getTrackedRace(), track.getTrackedItem());
                data.add(new GPSFixWithContextImpl(gpsFix, context));
            }
        } finally {
            track.unlockAfterRead();
        }
        return data;
    }

    protected Collection<GPSFixWithContext> legToGPSFixesWithContext(Leg leg) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
        for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
            GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrackedRace().getTrack(competitor);
            for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                GPSFixContext context = new GPSFixContextImpl(getTrackedRace(), competitor);
                data.add(new GPSFixWithContextImpl(gpsFix, context));
            }
        }
        return data;
    }

}
