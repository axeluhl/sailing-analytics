package com.sap.sailing.domain.tracking.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedLeg;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RawListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace, RawListener<Competitor, GPSFixMoving> {
    private final Set<RawListener<Competitor, GPSFixMoving>> listeners;
    
    public DynamicTrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race) {
        super(trackedEvent, race);
        listeners = new HashSet<RawListener<Competitor, GPSFixMoving>>();
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
    public void addListener(RawListener<Competitor, GPSFixMoving> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        for (RawListener<Competitor, GPSFixMoving> listener : listeners) {
            listener.gpsFixReceived(fix, competitor);
        }
    }

    @Override
    public void gpsFixReceived(GPSFixMoving fix, Competitor competitor) {
        notifyListeners(fix, this, competitor);
    }
    
    @Override
    public DynamicTrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        return (DynamicTrackedLeg) super.getTrackedLegFinishingAt(endOfLeg);
    }

    @Override
    public DynamicTrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        return (DynamicTrackedLeg) super.getTrackedLegStartingAt(startOfLeg);
    }
    
    @Override
    public DynamicTrackedLeg getTrackedLeg(Leg leg) {
        return (DynamicTrackedLeg) super.getTrackedLeg(leg);
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        clearMarkPassings(competitor);
        NavigableSet<MarkPassing> competitorMarkPassings = getMarkPassings(competitor);
        for (MarkPassing markPassing : markPassings) {
            competitorMarkPassings.add(markPassing);
            getMarkPassingsInOrder(markPassing.getWaypoint()).add(markPassing);
        }
    }
    
    @Override
    public Collection<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return (Collection<MarkPassing>) super.getMarkPassingsInOrder(waypoint);
    }

    private void clearMarkPassings(Competitor competitor) {
        Iterator<MarkPassing> mpIter = getMarkPassings(competitor).iterator();
        while (mpIter.hasNext()) {
            MarkPassing mp = mpIter.next();
            mpIter.remove();
            getMarkPassingsInOrder(mp.getWaypoint()).remove(mp);
        }
    }

    @Override
    public void setFirstFinish(TimePoint firstFinish) {
        super.setFirstFinish(firstFinish);
    }

    @Override
    public void setStart(TimePoint start) {
        super.setStart(start);
    }
}
