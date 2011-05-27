package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;

public class TrackedRaceImpl implements TrackedRace {
    private final TrackedEvent trackedEvent;
    private final RaceDefinition race;
    private TimePoint start;
    private TimePoint firstFinish;
    private TimePoint lastUpdate;
    
    /**
     * legs appear in the order in which they appear in the race's course
     */
    private final LinkedHashMap<Leg, TrackedLeg> trackedLegs;
    
    private final Map<Competitor, GPSFixTrack<Competitor, GPSFixMoving>> tracks;
    private final Map<Competitor, NavigableSet<MarkPassing>> markPassingsForCompetitor;
    private final Map<Waypoint, NavigableSet<MarkPassing>> markPassingsForWaypoint;
    private final WindTrack windTrack;
    
    public TrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.trackedEvent = trackedEvent;
        this.race = race;
        LinkedHashMap<Leg, TrackedLeg> trackedLegsMap = new LinkedHashMap<Leg, TrackedLeg>();
        for (Leg leg : race.getCourse().getLegs()) {
            trackedLegsMap.put(leg, new DynamicTrackedLegImpl(this, leg, race.getCompetitors()));
        }
        trackedLegs = trackedLegsMap;
        markPassingsForCompetitor = new HashMap<Competitor, NavigableSet<MarkPassing>>();
        tracks = new HashMap<Competitor, GPSFixTrack<Competitor, GPSFixMoving>>();
        for (Competitor competitor : race.getCompetitors()) {
            markPassingsForCompetitor.put(competitor, new TreeSet<MarkPassing>(TimedComparator.INSTANCE));
            tracks.put(competitor, new DynamicGPSFixMovingTrackImpl<Competitor>(competitor, millisecondsOverWhichToAverageSpeed));
        }
        markPassingsForWaypoint = new HashMap<Waypoint, NavigableSet<MarkPassing>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            markPassingsForWaypoint.put(waypoint, new TreeSet<MarkPassing>(TimedComparator.INSTANCE));
        }
        windTrack = new WindTrackImpl(millisecondsOverWhichToAverageWind);
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
    public GPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
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
        return getTrackedLeg(leg).getTrackedLeg(competitor);
    }

    @Override
    public TimePoint getTimePointOfLastUpdate() {
        return lastUpdate;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        int previousRank;
        if (leg == getRace().getCourse().getLegs().iterator().next()) {
            // first leg; report rank difference from 0
            previousRank = 0;
        } else {
            TrackedLeg previousLeg = getTrackedLegFinishingAt(leg.getFrom());
            previousRank = previousLeg.getTrackedLeg(competitor).getRank(timePoint);
        }
        int currentRank = getTrackedLeg(competitor, leg).getRank(timePoint);
        return currentRank - previousRank;
    }
    
    @Override
    public int getRank(Competitor competitor) {
        return getRank(competitor, MillisecondsTimePoint.now());
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        return getTrackedLeg(competitor, timePoint).getRank(timePoint);
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor) {
        NavigableSet<MarkPassing> competitorMarkPassings = markPassingsForCompetitor.get(competitor);
        TrackedLegOfCompetitor result = null;
        if (!competitorMarkPassings.isEmpty()) {
            result = getTrackedLegStartingAt(competitorMarkPassings.last().getWaypoint()).getTrackedLeg(competitor);
        }
        return result;
    }
    
    @Override
    public TrackedLeg getCurrentLeg() {
        Waypoint lastWaypointPassed = null;
        int indexOfLastWaypointPassed = -1;
        for (Map.Entry<Waypoint, NavigableSet<MarkPassing>> entry : markPassingsForWaypoint.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                int indexOfWaypoint = getRace().getCourse().getIndexOfWaypoint(entry.getKey());
                if (indexOfWaypoint > indexOfLastWaypointPassed) {
                    indexOfLastWaypointPassed = indexOfWaypoint;
                    lastWaypointPassed = entry.getKey();
                }
            }
        }
        TrackedLeg result = null;
        if (lastWaypointPassed != null && lastWaypointPassed != getRace().getCourse().getLastWaypoint()) {
            result = getTrackedLegStartingAt(lastWaypointPassed);
        }
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

    @Override
    public GPSFixTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
        TrackedEvent trackedEvent = getTrackedEvent();
        return trackedEvent.getTrack(buoy);
    }

    private TrackedEvent getTrackedEvent() {
        return trackedEvent;
    }
    
    protected WindTrack getWindTrack() {
        return windTrack;
    }

    @Override
    public Wind getWind(Position p, TimePoint at) {
        return getWindTrack().getEstimatedWind(p, at);
    }
    
    protected void updated(TimePoint timeOfEvent) {
        if (lastUpdate == null || timeOfEvent.compareTo(lastUpdate) > 0) {
            lastUpdate = timeOfEvent;
        }
    }
    
}
