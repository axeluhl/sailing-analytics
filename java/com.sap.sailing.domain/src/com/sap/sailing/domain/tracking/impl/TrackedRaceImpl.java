package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
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
import com.sap.sailing.domain.tracking.NoWindError;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.util.Util;

public abstract class TrackedRaceImpl implements TrackedRace, CourseListener {
    private static final Logger logger = Logger.getLogger(TrackedRaceImpl.class.getName());
    
    // TODO observe the race course; if it changes, update leg structures; consider fine-grained update events that tell what changed
    private final RaceDefinition race;
    
    /**
     * Keeps the oldest timestamp that is fed into this tracked race, either from a boat fix, a buoy
     * fix, a race start/finish or a coarse definition.
     */
    private TimePoint startOfTracking;
    
    /**
     * Race start time as announced by the tracking infrastructure
     */
    private TimePoint startTimeReceived;
    
    private TimePoint timePointOfNewestEvent;
    private TimePoint timePointOfLastEvent;
    private long updateCount;
    
    private final Map<TimePoint, List<Competitor>> competitorRankings; 
    
    /**
     * legs appear in the order in which they appear in the race's course
     */
    private final LinkedHashMap<Leg, TrackedLeg> trackedLegs;
    
    private final Map<Competitor, GPSFixTrack<Competitor, GPSFixMoving>> tracks;
    private final Map<Competitor, NavigableSet<MarkPassing>> markPassingsForCompetitor;
    
    /**
     * The mark passing sets used as values are ordered by time stamp.
     */
    private final Map<Waypoint, NavigableSet<MarkPassing>> markPassingsForWaypoint;
    
    /**
     * A tracked race can maintain a number of sources for wind information from which a client
     * can select. As all intra-leg computations are done dynamically based on wind information,
     * selecting a different wind information source can alter the intra-leg results. See
     * {@link #currentWindSource}.
     */
    private final Map<WindSource, WindTrack> windTracks;
    
    /**
     * The wind source to be used for all computations based on wind. Used as key into
     * {@link #windTracks}. The default value is {@link WindSource#EXPEDITION}.
     */
    private WindSource currentWindSource;

    private final Map<Buoy, GPSFixTrack<Buoy, GPSFix>> buoyTracks;
    
    private final long millisecondsOverWhichToAverageSpeed;
    
    public TrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.updateCount = 0;
        this.race = race;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
        this.buoyTracks = new HashMap<Buoy, GPSFixTrack<Buoy, GPSFix>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                if (!buoyTracks.containsKey(buoy)) {
                    buoyTracks.put(buoy, new DynamicTrackImpl<Buoy, GPSFix>(buoy, millisecondsOverWhichToAverageSpeed));
                }
            }
        }
        trackedLegs = new LinkedHashMap<Leg, TrackedLeg>();
        for (Leg leg : race.getCourse().getLegs()) {
            trackedLegs.put(leg, createTrackedLeg(leg));
        }
        markPassingsForCompetitor = new HashMap<Competitor, NavigableSet<MarkPassing>>();
        tracks = new HashMap<Competitor, GPSFixTrack<Competitor, GPSFixMoving>>();
        for (Competitor competitor : race.getCompetitors()) {
            markPassingsForCompetitor.put(competitor, new ConcurrentSkipListSet<MarkPassing>(TimedComparator.INSTANCE));
            tracks.put(competitor, new DynamicGPSFixMovingTrackImpl<Competitor>(competitor, millisecondsOverWhichToAverageSpeed));
        }
        markPassingsForWaypoint = new HashMap<Waypoint, NavigableSet<MarkPassing>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            markPassingsForWaypoint.put(waypoint, new ConcurrentSkipListSet<MarkPassing>(TimedComparator.INSTANCE));
        }
        windTracks = new HashMap<WindSource, WindTrack>();
        for (WindSource windSource : WindSource.values()) {
            windTracks.put(windSource, windStore.getWindTrack(trackedEvent, this, windSource, millisecondsOverWhichToAverageWind));
        }
        currentWindSource = WindSource.EXPEDITION;
        competitorRankings = new HashMap<TimePoint, List<Competitor>>();
        getRace().getCourse().addCourseListener(this);
    }

    /**
     * Precondition: race has already been set, e.g., in constructor before this methocd is called
     */
    abstract protected TrackedLeg createTrackedLeg(Leg leg);
    
    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        return markPassingsForCompetitor.get(competitor);
    }
    
    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return markPassingsForWaypoint.get(waypoint);
    }
    
    @Override
    public TimePoint getStartOfTracking() {
        return startOfTracking;
    }

    @Override
    public TimePoint getStart() {
        TimePoint result;
        Iterator<MarkPassing> markPassingsFirstMarkIter = getMarkPassingsInOrder(getRace().getCourse().getWaypoints().iterator().next()).iterator();
        if (markPassingsFirstMarkIter.hasNext()) {
            MarkPassing firstMarkPassingFirstMark = markPassingsFirstMarkIter.next();
            TimePoint timeOfFirstMarkPassingFirstMark = firstMarkPassingFirstMark.getTimePoint();
            if (startTimeReceived != null) {
                long startTimeReceived2timeOfFirstMarkPassingFirstMark = timeOfFirstMarkPassingFirstMark.asMillis() -
                        startTimeReceived.asMillis();
                if (startTimeReceived2timeOfFirstMarkPassingFirstMark > MAX_TIME_BETWEEN_START_AND_FIRST_MARK_PASSING_IN_MILLISECONDS) {
                    result = new MillisecondsTimePoint(timeOfFirstMarkPassingFirstMark.asMillis() - MAX_TIME_BETWEEN_START_AND_FIRST_MARK_PASSING_IN_MILLISECONDS);
                } else {
                    result = startTimeReceived;
                }
            } else {
                result = timeOfFirstMarkPassingFirstMark;
            }
        } else {
            result = startTimeReceived;
        }
        return result;
    }

    protected void setStartTimeReceived(TimePoint start) {
        this.startTimeReceived = start;
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
        } else if (indexOfWaypoint == Util.size(getRace().getCourse().getWaypoints())-1) {
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
    public long getUpdateCount() {
        return updateCount;
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
    public synchronized int getRank(Competitor competitor) throws NoWindException {
        return getRank(competitor, MillisecondsTimePoint.now());
    }

    @Override
    public synchronized int getRank(Competitor competitor, TimePoint timePoint) throws NoWindException {
        try {
            synchronized (competitorRankings) {
                List<Competitor> rankedCompetitors = competitorRankings.get(timePoint);
                if (rankedCompetitors == null) {
                    RaceRankComparator comparator = new RaceRankComparator(this, timePoint);
                    rankedCompetitors = new ArrayList<Competitor>();
                    for (Competitor c : getRace().getCompetitors()) {
                        rankedCompetitors.add(c);
                    }
                    Collections.sort(rankedCompetitors, comparator);
                    competitorRankings.put(timePoint, rankedCompetitors);
                }
                return rankedCompetitors.indexOf(competitor)+1;
            }
        } catch (NoWindError e) {
            throw e.getCause();
        }
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint) {
        NavigableSet<MarkPassing> competitorMarkPassings = markPassingsForCompetitor.get(competitor);
        DummyMarkPassingWithTimePointOnly markPassingTimePoint = new DummyMarkPassingWithTimePointOnly(timePoint);
        TrackedLegOfCompetitor result = null;
        if (!competitorMarkPassings.isEmpty()) {
            MarkPassing lastMarkPassingAtOfBeforeTimePoint = competitorMarkPassings.floor(markPassingTimePoint);
            if (lastMarkPassingAtOfBeforeTimePoint != null) {
                Waypoint waypointPassedLastAtOrBeforeTimePoint = lastMarkPassingAtOfBeforeTimePoint.getWaypoint();
                // don't return a leg if competitor has already finished last leg and therefore the race
                if (waypointPassedLastAtOrBeforeTimePoint != getRace().getCourse().getLastWaypoint()) {
                    result = getTrackedLegStartingAt(waypointPassedLastAtOrBeforeTimePoint).getTrackedLeg(competitor);
                }
            }
        }
        return result;
    }
    
    @Override
    public TrackedLeg getCurrentLeg(TimePoint timePoint) {
        Waypoint lastWaypointPassed = null;
        int indexOfLastWaypointPassed = -1;
        for (Map.Entry<Waypoint, NavigableSet<MarkPassing>> entry : markPassingsForWaypoint.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                MarkPassing first = entry.getValue().first();
                // Did the mark passing happen at or before the requested time point?
                if (first.getTimePoint().compareTo(timePoint) <= 0) {
                    int indexOfWaypoint = getRace().getCourse().getIndexOfWaypoint(entry.getKey());
                    if (indexOfWaypoint > indexOfLastWaypointPassed) {
                        indexOfLastWaypointPassed = indexOfWaypoint;
                        lastWaypointPassed = entry.getKey();
                    }
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
        synchronized (buoyTracks) {
            GPSFixTrack<Buoy, GPSFix> result = buoyTracks.get(buoy);
            if (result == null) {
                result = new DynamicTrackImpl<Buoy, GPSFix>(buoy, millisecondsOverWhichToAverageSpeed);
                buoyTracks.put(buoy, result);
            }
            return result;
        }
    }

    @Override
    public WindTrack getWindTrack(WindSource windSource) {
        return windTracks.get(windSource);
    }

    @Override
    public Wind getWind(Position p, TimePoint at) {
        Wind result = getWindTrack(currentWindSource).getEstimatedWind(p, at);
        if (result == null) {
            logger.warning("Couldn't find any wind information for race "+getRace()+" from currently selected source "+currentWindSource);
            for (WindSource alternativeWindSource : WindSource.values()) {
                if (alternativeWindSource != currentWindSource) {
                    result = getWindTrack(alternativeWindSource).getEstimatedWind(p, at);
                    if (result != null) {
                        logger.warning("Found wind settings in alternative wind source "+alternativeWindSource+" which will be used as a fallback");
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public WindSource getWindSource() {
        return this.currentWindSource;
    }
    
    @Override
    public void setWindSource(WindSource windSource) {
        this.currentWindSource = windSource;
    }

    @Override
    public TimePoint getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }
    
    @Override
    public TimePoint getTimePointOfLastEvent() {
        return timePointOfLastEvent;
    }

    /**
     * @param timeOfEvent may be <code>null</code> meaning to only unblock waiters but not update any time points
     */
    protected synchronized void updated(TimePoint timeOfEvent) {
        updateCount++;
        clearAllCaches();
        if (timeOfEvent != null) {
            if (timePointOfNewestEvent == null || timePointOfNewestEvent.compareTo(timeOfEvent) < 0) {
                timePointOfNewestEvent = timeOfEvent;
            }
            if (startOfTracking == null || startOfTracking.compareTo(timeOfEvent) > 0) {
                startOfTracking = timeOfEvent;
            }
            timePointOfLastEvent = timeOfEvent;
        }
        notifyAll();
    }

    private synchronized void clearAllCaches() {
        synchronized (competitorRankings) {
            competitorRankings.clear();
        }
    }

    @Override
    public synchronized void waitForNextUpdate(int sinceUpdate) throws InterruptedException {
        while (updateCount <= sinceUpdate) {
            wait(); // ...until updated(...) notifies us
        }
    }
    

    @Override
    public synchronized void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        markPassingsForWaypoint.put(waypointThatGotAdded, new ConcurrentSkipListSet<MarkPassing>(TimedComparator.INSTANCE));
        for (Buoy buoy : waypointThatGotAdded.getBuoys()) {
            if (!buoyTracks.containsKey(buoy)) {
                buoyTracks.put(buoy, new DynamicTrackImpl<Buoy, GPSFix>(buoy, millisecondsOverWhichToAverageSpeed));
            }
        }
        // a waypoint got added; this means that a leg got added as well; but we shouldn't claim we know where
        // in the leg list of the course the leg was added; that's an implementation secret of CourseImpl. So try:
        LinkedHashMap<Leg, TrackedLeg> reorderedTrackedLegs = new LinkedHashMap<Leg, TrackedLeg>();
        for (Leg leg : getRace().getCourse().getLegs()) {
            if (!trackedLegs.containsKey(leg)) {
                // no tracked leg for leg yet:
                TrackedLeg newTrackedLeg = createTrackedLeg(leg);
                reorderedTrackedLegs.put(leg, newTrackedLeg);
            } else {
                reorderedTrackedLegs.put(leg, trackedLegs.get(leg));
            }
        }
        // now ensure that the iteration order is in sync with the leg iteration order
        trackedLegs.clear();
        for (Map.Entry<Leg, TrackedLeg> entry : reorderedTrackedLegs.entrySet()) {
            trackedLegs.put(entry.getKey(), entry.getValue());
        }
        updated(/* time point*/ null);
    }

    @Override
    public synchronized void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        Leg toRemove = null;
        Leg last = null;
        int i=0;
        for (Map.Entry<Leg, TrackedLeg> e : trackedLegs.entrySet()) {
            last = e.getKey();
            if (i == zeroBasedIndex) {
                toRemove = e.getKey();
                break;
            }
            i++;
        }
        if (toRemove == null) {
            // last waypoint removed
            toRemove = last;
        }
        trackedLegs.remove(toRemove);
        updated(/* time point*/ null);
    }
}
