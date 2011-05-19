package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceImpl implements TrackedRace {
    private final RaceDefinition race;
    private TimePoint start;
    private TimePoint firstFinish;
    
    /**
     * legs appear in the order in which they appear in the race's course
     */
    private final LinkedHashMap<Leg, TrackedLeg> trackedLegs;
    
    private final Map<Competitor, Track<Competitor, GPSFixMoving>> tracks;
    private final Map<Competitor, NavigableSet<MarkPassing>> markRoundings;
    
    public TrackedRaceImpl(RaceDefinition race) {
        super();
        this.race = race;
        LinkedHashMap<Leg, TrackedLeg> trackedLegsMap = new LinkedHashMap<Leg, TrackedLeg>();
        for (Leg leg : race.getCourse().getLegs()) {
            trackedLegsMap.put(leg, new DynamicTrackedLegImpl(this, leg, race.getCompetitors()));
        }
        trackedLegs = trackedLegsMap;
        markRoundings = new HashMap<Competitor, NavigableSet<MarkPassing>>();
        tracks = new HashMap<Competitor, Track<Competitor, GPSFixMoving>>();
        for (Competitor competitor : race.getCompetitors()) {
            markRoundings.put(competitor, new TreeSet<MarkPassing>(TimedComparator.INSTANCE));
            tracks.put(competitor, new DynamicTrackImpl<Competitor, GPSFixMoving>(competitor));
        }
    }

    @Override
    public TimePoint getStart() {
        return start;
    }


    public void setStart(TimePoint start) {
        this.start = start;
    }


    @Override
    public TimePoint getFirstFinish() {
        return firstFinish;
    }


    public void setFirstFinish(TimePoint firstFinish) {
        this.firstFinish = firstFinish;
    }


    @Override
    public RaceDefinition getRace() {
        return race;
    }


    @Override
    public Iterable<TrackedLeg> getTrackedLegs() {
        return trackedLegs.values();
    }

    @Override
    public Track<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return tracks.get(competitor);
    }

    @Override
    public TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        int indexOfWaypoint = getRace().getCourse().getIndexOfWaypoint(endOfLeg);
        if (indexOfWaypoint == -1) {
            throw new IllegalArgumentException("Waypoint "+endOfLeg+" not found in "+getRace().getCourse());
        } else if (indexOfWaypoint == 0) {
            throw new IllegalArgumentException("Waypoint "+endOfLeg+" isn't start of any leg in "+getRace().getCourse());
        }
        return trackedLegs.get(race.getCourse().getLegs().get(indexOfWaypoint-1));
    }

    @Override
    public TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        int indexOfWaypoint = getRace().getCourse().getIndexOfWaypoint(startOfLeg);
        if (indexOfWaypoint == -1) {
            throw new IllegalArgumentException("Waypoint "+startOfLeg+" not found in "+getRace().getCourse());
        } else if (indexOfWaypoint == trackedLegs.size()-1) {
            throw new IllegalArgumentException("Waypoint "+startOfLeg+" isn't start of any leg in "+getRace().getCourse());
        }
        return trackedLegs.get(race.getCourse().getLegs().get(indexOfWaypoint));
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at) {
        NavigableSet<MarkPassing> roundings = markRoundings.get(competitor);
        MarkPassing lastBeforeOrAt = roundings.floor(new DummyMarkPassingWithTimePointOnly(at));
        TrackedLegOfCompetitor result = null;
        if (lastBeforeOrAt != null) {
            TrackedLeg trackedLeg = getTrackedLegStartingAt(lastBeforeOrAt.getWaypoint());
            result = trackedLeg.getTrackedLeg(competitor);
        }
        return result;
    }
    
    protected TrackedLeg getTrackedLeg(Leg leg) {
        return trackedLegs.get(leg);
    }

}
