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
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceDataRetriever implements DataRetriever {

    private TrackedRace trackedRace;

    public TrackedRaceDataRetriever(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    protected TrackedRace getTrackedRace() {
        return trackedRace;
    }
    
    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        int legNumber = 1;
        for (Leg leg : getTrackedRace().getRace().getCourse().getLegs()) {
            try {
                LegType legType = getLegType(leg);
                TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
                if (trackedLeg != null) {
                    for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
                        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                        if (trackedLegOfCompetitor != null && retrieveDataFor(legNumber, legType, competitor)) {
                            data.addAll(retrieveDataFrom(trackedLegOfCompetitor, legNumber, legType));
                        }
                    }
                }
            } catch (NoWindException e) { }
            legNumber++;
        }
        return data;
    }

    private boolean retrieveDataFor(int legNumber, LegType legType, Competitor competitor) {
        return retrieveDataFor(legNumber) && retrieveDataFor(legType) && retrieveDataFor(competitor);
    }

    protected boolean retrieveDataFor(Competitor competitor) {
        return true;
    }
    
    protected boolean retrieveDataFor(LegType legType) {
        return true;
    }
    
    protected boolean retrieveDataFor(int legNumber) {
        return true;
    }

    private Collection<GPSFixWithContext> retrieveDataFrom(TrackedLegOfCompetitor trackedLegOfCompetitor, int legNumber, LegType legType) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrackedRace().getTrack(trackedLegOfCompetitor.getCompetitor());
        competitorTrack.lockForRead();
        try {
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null) {
                for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                    GPSFixContext context = new GPSFixContextImpl(getTrackedRace(), legNumber, legType, trackedLegOfCompetitor.getCompetitor());
                    data.add(new GPSFixWithContextImpl(gpsFix, context));
                }
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        return data;
    }

    private LegType getLegType(Leg leg) throws NoWindException {
        TimePoint at = null;
        TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
        for (TrackedLegOfCompetitor trackedLegOfCompetitor : trackedLeg.getTrackedLegsOfCompetitors()) {
            TimePoint start = trackedLegOfCompetitor.getStartTime();
            TimePoint finish = trackedLegOfCompetitor.getFinishTime();
            if (start != null && finish != null) {
                at = new MillisecondsTimePoint((start.asMillis() + finish.asMillis()) / 2);
                break;
            }
        }
        return trackedLeg.getLegType(at);
    }

}
