package com.sap.sailing.domain.tracking.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedLeg;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.RawListener;
import com.sap.sailing.domain.tracking.TrackedRace;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace, RawListener<GPSFixMoving> {
    private final Set<RawListener<GPSFixMoving>> listeners;
    
    public DynamicTrackedRaceImpl(RaceDefinition race) {
        super(race);
        listeners = new HashSet<RawListener<GPSFixMoving>>();
        for (Competitor competitor : getRace().getCompetitors()) {
            DynamicTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
            track.addListener(this);
        }
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        DynamicTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
        track.addGPSFix(fix);
    }

    @Override
    public DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
    }

    @Override
    public void addListener(RawListener<GPSFixMoving> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        for (RawListener<GPSFixMoving> listener : listeners) {
            listener.gpsFixReceived(fix, trackedRace, competitor);
        }
    }

    @Override
    public void gpsFixReceived(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        notifyListeners(fix, trackedRace, competitor);
    }
    
    @Override
    public DynamicTrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        return (DynamicTrackedLeg) super.getTrackedLegFinishingAt(endOfLeg);
    }

    @Override
    public DynamicTrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        return (DynamicTrackedLeg) super.getTrackedLegStartingAt(startOfLeg);
    }

}
