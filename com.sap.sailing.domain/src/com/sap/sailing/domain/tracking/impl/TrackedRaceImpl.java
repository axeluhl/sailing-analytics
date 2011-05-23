package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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
    private final Map<Competitor, NavigableSet<MarkPassing>> markPassingsForCompetitor;
    private final Map<Waypoint, NavigableSet<MarkPassing>> markPassingsForWaypoint;
    
    public TrackedRaceImpl(RaceDefinition race) {
        super();
        this.race = race;
        LinkedHashMap<Leg, TrackedLeg> trackedLegsMap = new LinkedHashMap<Leg, TrackedLeg>();
        for (Leg leg : race.getCourse().getLegs()) {
            trackedLegsMap.put(leg, new DynamicTrackedLegImpl(this, leg, race.getCompetitors()));
        }
        trackedLegs = trackedLegsMap;
        markPassingsForCompetitor = new HashMap<Competitor, NavigableSet<MarkPassing>>();
        tracks = new HashMap<Competitor, Track<Competitor, GPSFixMoving>>();
        for (Competitor competitor : race.getCompetitors()) {
            markPassingsForCompetitor.put(competitor, new TreeSet<MarkPassing>(TimedComparator.INSTANCE));
            tracks.put(competitor, new DynamicTrackImpl<Competitor, GPSFixMoving>(competitor));
        }
        markPassingsForWaypoint = new HashMap<Waypoint, NavigableSet<MarkPassing>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            markPassingsForWaypoint.put(waypoint, new TreeSet<MarkPassing>(TimedComparator.INSTANCE));
        }
    }
    
    protected NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        return markPassingsForCompetitor.get(competitor);
    }
    
    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return markPassingsForWaypoint.get(waypoint);
    }

    @Override
    public TimePoint getStart() {
        return start;
    }

    protected void setStart(TimePoint start) {
        this.start = start;
    }

    @Override
    public TimePoint getFirstFinish() {
        return firstFinish;
    }

    protected void setFirstFinish(TimePoint firstFinish) {
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
        NavigableSet<MarkPassing> roundings = markPassingsForCompetitor.get(competitor);
        MarkPassing lastBeforeOrAt = roundings.floor(new DummyMarkPassingWithTimePointOnly(at));
        TrackedLegOfCompetitor result = null;
        if (lastBeforeOrAt != null) {
            TrackedLeg trackedLeg = getTrackedLegStartingAt(lastBeforeOrAt.getWaypoint());
            result = trackedLeg.getTrackedLeg(competitor);
        }
        return result;
    }
    
    public TrackedLeg getTrackedLeg(Leg leg) {
        return trackedLegs.get(leg);
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfLastUpdate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int getRank(Competitor competitor) {
        return getRank(competitor, MillisecondsTimePoint.now());
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, Waypoint waypoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor) {
        TrackedLegOfCompetitor result = null;
        // TODO
//        for (TrackedLeg l : trackedLegs.values()) {
//            result = l;
//        }
        return result;
    }

    @Override
    public Distance getStartAdvantage(Competitor competitor, double secondsIntoTheRace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint) {
        for (MarkPassing markPassing : getMarkPassings(competitor)) {
            if (markPassing.getWaypoint() == waypoint) {
                return markPassing;
            }
        }
        return null;
    }

}
