package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;

public class SelectorImpl implements Selector {
    
    private SelectionCriteria criteria;

    public SelectorImpl(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Regatta regatta : racingEventService.getAllRegattas()) {
            TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                    if (trackedRace != null) {
                        int legNumber = 1;
                        for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                                SelectionContext context = new SelectionContextImpl(trackedRegatta, trackedRace, legNumber, competitor);
                                if (criteria.matches(context)) {
                                    data.addAll(retrieveDataFor(trackedRace, leg, legNumber, competitor));
                                }
                            }
                            legNumber++;
                        }
                    }
                }
            }
        }
        return data;
    }

    private Collection<GPSFixWithContext> retrieveDataFor(TrackedRace trackedRace, Leg leg, int legNumber, Competitor competitor) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, leg);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
        competitorTrack.lockForRead();
        try {
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null) {
                for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                    data.add(new GPSFixWithContextImpl(gpsFix, trackedRace, leg, legNumber, competitor));
                }
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        return data;
    }

}
