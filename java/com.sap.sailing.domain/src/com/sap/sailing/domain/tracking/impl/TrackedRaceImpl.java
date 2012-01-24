package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DouglasPeucker;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RacePlaceOrder;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.RacePlaceOrderImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.geocoding.ReverseGeocoder;

public abstract class TrackedRaceImpl implements TrackedRace, CourseListener {
    private static final Logger logger = Logger.getLogger(TrackedRaceImpl.class.getName());

    private static final double PENALTY_CIRCLE_DEGREES_THRESHOLD = 320;
    
    /**
     * Used in {@link #getPlaceOrder()} to calculate the radius for the
     * {@link ReverseGeocoder#getPlacemarksNear(Position, double) GetPlacemarksNear-Service}.
     */
    private static final double GEONAMES_RADIUS_CACLCULATION_FACTOR = 10.0;

    // TODO observe the race course; if it changes, update leg structures; consider fine-grained update events that tell what changed
    private final RaceDefinition race;
    
    private final TrackedEvent trackedEvent;
    
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
     * Values are the <code>from</code> and <code>to</code> time points between which the maneuvers have been previously
     * computed. Clients wanting to know maneuvers for the competitor outside of this time interval need to (re-)compute them.
     */
    private final Map<Competitor, Triple<TimePoint, TimePoint, List<Maneuver>>> maneuverCache;
    
    /**
     * A tracked race can maintain a number of sources for wind information from which a client
     * can select. As all intra-leg computations are done dynamically based on wind information,
     * selecting a different wind information source can alter the intra-leg results. See
     * {@link #currentWindSource}.
     */
    private final Map<WindSource, WindTrack> windTracks;
    
    private Wind directionFromStartToNextMarkCache;
    
    private final Map<Buoy, GPSFixTrack<Buoy, GPSFix>> buoyTracks;
    
    private final long millisecondsOverWhichToAverageSpeed;

    private final Map<Buoy, StartToNextMarkCacheInvalidationListener> startToNextMarkCacheInvalidationListeners;
    
    public TrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.updateCount = 0;
        this.race = race;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
        this.startToNextMarkCacheInvalidationListeners = new HashMap<Buoy, TrackedRaceImpl.StartToNextMarkCacheInvalidationListener>();
        this.maneuverCache = new HashMap<Competitor, Util.Triple<TimePoint,TimePoint,List<Maneuver>>>();
        this.buoyTracks = new HashMap<Buoy, GPSFixTrack<Buoy, GPSFix>>();
        int i = 0;
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                getOrCreateTrack(buoy);
                if (i<2) {
                    // add cache invalidation listeners for first and second waypoint's buoys for directionFromStartToNextMarkCache
                    addStartToNextMarkCacheInvalidationListener(buoy);
                }
            }
            i++;
        }
        trackedLegs = new LinkedHashMap<Leg, TrackedLeg>();
        synchronized (race.getCourse()) {
            for (Leg leg : race.getCourse().getLegs()) {
                trackedLegs.put(leg, createTrackedLeg(leg));
            }
            getRace().getCourse().addCourseListener(this);
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
        this.trackedEvent = trackedEvent;
        competitorRankings = new HashMap<TimePoint, List<Competitor>>();
    }
    
    /**
     * Precondition: race has already been set, e.g., in constructor before this methocd is called
     */
    abstract protected TrackedLeg createTrackedLeg(Leg leg);
    
    public RaceIdentifier getRaceIdentifier() {
        return new EventNameAndRaceName(getTrackedEvent().getEvent().getName(), getRace().getName());
    }
    
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
    
    @Override
    public boolean hasStarted(TimePoint at) {
        return getStart() != null && getStart().compareTo(at) <= 0;
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
        // already finished the race?
        if (lastBeforeOrAt != null && getRace().getCourse().getLastWaypoint() != lastBeforeOrAt.getWaypoint()) {
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
    public GPSFixTrack<Buoy, GPSFix> getOrCreateTrack(Buoy buoy) {
        synchronized (buoyTracks) {
            GPSFixTrack<Buoy, GPSFix> result = buoyTracks.get(buoy);
            if (result == null) {
                result = createBuoyTrack(buoy);
                buoyTracks.put(buoy, result);
            }
            return result;
        }
    }

    protected DynamicGPSFixTrackImpl<Buoy> createBuoyTrack(Buoy buoy) {
        return new DynamicGPSFixTrackImpl<Buoy>(buoy, millisecondsOverWhichToAverageSpeed);
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint) {
        Position result = null;
        for (Buoy buoy : waypoint.getBuoys()) {
            Position nextPos = getOrCreateTrack(buoy).getEstimatedPosition(timePoint, /* extrapolate */ false);
            if (result == null) {
                result = nextPos;
            } else {
                result = result.translateGreatCircle(result.getBearingGreatCircle(nextPos), result.getDistance(nextPos).scale(0.5));
            }
        }
        return result;
    }

    @Override
    public WindTrack getWindTrack(WindSource windSource) {
        return windTracks.get(windSource);
    }

    @Override
    public Wind getWind(Position p, TimePoint at, WindSource... windSourcesToExclude) {
        List<WindSource> windSourcesToConsider = new ArrayList<WindSource>();
        windSourcesToConsider.add(getWindSource());
        for (WindSource windSource : WindSource.values()) {
            if (windSource != getWindSource()) {
                windSourcesToConsider.add(windSource);
            }
        }
        for (WindSource windSourceToExclude : windSourcesToExclude) {
            windSourcesToConsider.remove(windSourceToExclude);
        }
        for (WindSource windSource : windSourcesToConsider) {
            Wind result = getWindTrack(windSource).getEstimatedWind(p, at);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Wind getDirectionFromStartToNextMark(TimePoint at) {
        Wind result = directionFromStartToNextMarkCache;
        if (result == null) {
            Leg firstLeg = getRace().getCourse().getLegs().iterator().next();
            Position firstLegEnd = getApproximatePosition(firstLeg.getTo(), at);
            Position firstLegStart = getApproximatePosition(firstLeg.getFrom(), at);
            if (firstLegStart != null && firstLegEnd != null) {
                result = new WindImpl(firstLegStart, at, new KnotSpeedWithBearingImpl(1.0,
                        firstLegEnd.getBearingGreatCircle(firstLegStart)));
                final Wind finalResult = result;
                directionFromStartToNextMarkCache = finalResult;
            } else {
                result = null;
            }
        }
        return result;
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
        synchronized (maneuverCache) {
            maneuverCache.clear();
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
        updateStartToNextMarkCacheInvalidationCacheListenersAfterWaypointAdded(zeroBasedIndex, waypointThatGotAdded);
        markPassingsForWaypoint.put(waypointThatGotAdded, new ConcurrentSkipListSet<MarkPassing>(TimedComparator.INSTANCE));
        for (Buoy buoy : waypointThatGotAdded.getBuoys()) {
            getOrCreateTrack(buoy);
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

    private void updateStartToNextMarkCacheInvalidationCacheListenersAfterWaypointAdded(int zeroBasedIndex,
            Waypoint waypointThatGotAdded) {
        if (zeroBasedIndex < 2) {
            // the observing listener on any previous buoy will be GCed; we need to ensure
            // that the cache is recomputed
            directionFromStartToNextMarkCache = null;
            Iterator<Waypoint> waypointsIter = getRace().getCourse().getWaypoints().iterator();
            waypointsIter.next(); // skip first
            if (waypointsIter.hasNext()) {
                waypointsIter.next(); // skip second
                if (waypointsIter.hasNext()) {
                    Waypoint oldSecond = waypointsIter.next();
                    stopAndRemoveStartToNextMarkCacheInvalidationListener(oldSecond);
                }
            }
        }
        addStartToNextMarkCacheInvalidationListener(waypointThatGotAdded);
    }
    
    private void addStartToNextMarkCacheInvalidationListener(Waypoint waypoint) {
        for (Buoy buoy : waypoint.getBuoys()) {
            addStartToNextMarkCacheInvalidationListener(buoy);
        }
    }

    private void addStartToNextMarkCacheInvalidationListener(Buoy buoy) {
        GPSFixTrack<Buoy, GPSFix> track = getOrCreateTrack(buoy);
        StartToNextMarkCacheInvalidationListener listener = new StartToNextMarkCacheInvalidationListener(track);
        startToNextMarkCacheInvalidationListeners.put(buoy, listener);
        track.addListener(listener);
    }

    private void stopAndRemoveStartToNextMarkCacheInvalidationListener(Waypoint waypoint) {
        for (Buoy buoy : waypoint.getBuoys()) {
            stopAndRemoveStartToNextMarkCacheInvalidationListener(buoy);
        }
    }

    private void stopAndRemoveStartToNextMarkCacheInvalidationListener(Buoy buoy) {
        StartToNextMarkCacheInvalidationListener listener = startToNextMarkCacheInvalidationListeners.get(buoy);
        if (listener != null) {
            listener.stopListening();
            startToNextMarkCacheInvalidationListeners.remove(buoy);
        }
    }

    @Override
    public synchronized void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        updateStartToNextMarkCacheInvalidationCacheListenersAfterWaypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
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
        if (toRemove == null && !trackedLegs.isEmpty()) {
            // last waypoint removed
            toRemove = last;
        }
        if (toRemove != null) {
            trackedLegs.remove(toRemove);
            updated(/* time point*/ null);
        }
    }
    
    private void updateStartToNextMarkCacheInvalidationCacheListenersAfterWaypointRemoved(int zeroBasedIndex,
            Waypoint waypointThatGotRemoved) {
        if (zeroBasedIndex < 2) {
            // the observing listener on any previous buoy will be GCed; we need to ensure
            // that the cache is recomputed
            directionFromStartToNextMarkCache = null;
            stopAndRemoveStartToNextMarkCacheInvalidationListener(waypointThatGotRemoved);
            Iterator<Waypoint> waypointsIter = getRace().getCourse().getWaypoints().iterator();
            waypointsIter.next(); // skip first
            if (waypointsIter.hasNext()) {
                waypointsIter.next(); // skip second
                if (waypointsIter.hasNext()) {
                    Waypoint newSecond = waypointsIter.next();
                    addStartToNextMarkCacheInvalidationListener(newSecond);
                }
            }
        }
    }

    @Override
    public TrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public Wind getEstimatedWindDirection(Position position, TimePoint timePoint) {
        Map<LegType, BearingCluster> bearings = new HashMap<LegType, BearingCluster>();
        for (LegType legType : LegType.values()) {
            bearings.put(legType, new BearingCluster());
        }
        for (Competitor competitor : getRace().getCompetitors()) {
            TrackedLegOfCompetitor leg = getTrackedLeg(competitor, timePoint);
            if (leg != null) {
                TrackedLeg trackedLeg = getTrackedLeg(leg.getLeg());
                LegType legType;
                try {
                    legType = trackedLeg.getLegType(timePoint);
                } catch (NoWindException e) {
                    logger.warning("Unable to determine leg type for race "+getRace().getName()+" while trying to estimate wind");
                    return null;
                }
                if (legType != LegType.REACHING) {
                    GPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
                    // TODO bug #167 could be fixed here by excluding competitors that are x seconds before/after a mark passing
                    if (!track.hasDirectionChange(timePoint, getManeuverDegreeAngleThreshold())) {
                        SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(timePoint);
                        if (estimatedSpeed != null) {
                            Bearing bearing = estimatedSpeed.getBearing();
                            BearingCluster bearingClusters = bearings.get(legType);
                            bearingClusters.add(bearing);
                        }
                    }
                }
            }
        }
        Bearing reversedUpwindAverage = null;
        int upwindConfidence = 0;
        BearingCluster[] bearingClustersUpwind = bearings.get(LegType.UPWIND).splitInTwo(getMinimumAngleBetweenDifferentTacksUpwind());
        if (!bearingClustersUpwind[0].isEmpty() && !bearingClustersUpwind[1].isEmpty()) {
            reversedUpwindAverage = bearingClustersUpwind[0].getAverage().middle(bearingClustersUpwind[1].getAverage()).reverse();
            upwindConfidence = Math.min(bearingClustersUpwind[0].size(), bearingClustersUpwind[1].size());
        }
        Bearing downwindAverage = null;
        int downwindConfidence = 0;
        BearingCluster[] bearingClustersDownwind = bearings.get(LegType.DOWNWIND).splitInTwo(getMinimumAngleBetweenDifferentTacksDownwind());
        if (!bearingClustersDownwind[0].isEmpty() && !bearingClustersDownwind[1].isEmpty()) {
            downwindAverage = bearingClustersDownwind[0].getAverage().middle(bearingClustersDownwind[1].getAverage());
            downwindConfidence = Math.min(bearingClustersDownwind[0].size(), bearingClustersDownwind[1].size());
        }
        int confidence = upwindConfidence + downwindConfidence;
        BearingCluster resultCluster = new BearingCluster();
        assert upwindConfidence == 0 || reversedUpwindAverage != null;
        for (int i=0; i<upwindConfidence; i++) {
            resultCluster.add(reversedUpwindAverage);
        }
        assert downwindConfidence == 0 || downwindAverage != null;
        for (int i=0; i<downwindConfidence; i++) {
            resultCluster.add(downwindAverage);
        }
        Bearing resultBearing = resultCluster.getAverage();
        return resultBearing == null ? null : new WindImpl(null, timePoint,
                new KnotSpeedWithBearingImpl(/* speedInKnots */ confidence, resultBearing));
    }
    
    /**
     * This is probably best explained by example. If the wind bearing is from port to starboard, the situation looks
     * like this:
     * 
     * <pre>
     *                                 ^
     *                 Wind            | Boat
     *               ----------->      |
     *                                 |
     * 
     * </pre>
     * 
     * In this case, the boat's sails will be on the starboard side, so the result has to be {@link Tack#STARBOARD}. The
     * angle between the boat's heading (which we can only approximate by the boat's bearing) and the wind bearing in
     * this case is 90 degrees. <code>wind.{@link Bearing#getDifferenceTo(Bearing) getDifferenceTo}(boat)</code>
     * in this case will return a bearing representing -90 degrees.<p>
     * 
     * If the wind is blowing the other way, the angle returned by {@link Bearing#getDifferenceTo(Bearing)} will correspond
     * to +90 degrees. In other words, a negative angle means starboard tack, a positive angle represents port tack.<p>
     * 
     * For the unlikely case of 0 degrees difference, {@link Tack#STARBOARD} will result.
     */
    @Override
    public Tack getTack(Competitor competitor, TimePoint timePoint) {
        return getTack(getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolate */false), timePoint,
                getTrack(competitor).getEstimatedSpeed(timePoint).getBearing());
    }
    
    /**
     * Based on the wind direction at <code>timePoint</code> and at position <code>where</code>, compares the <code>boatBearing</code>
     * to the wind's bearing at that time and place and determined the tack.
     */
    private Tack getTack(Position where, TimePoint timePoint, Bearing boatBearing) {
        Bearing wind = getWind(where, timePoint).getBearing();
        Bearing difference = wind.getDifferenceTo(boatBearing);
        return difference.getDegrees() <= 0 ? Tack.STARBOARD : Tack.PORT;
    }

    @Override
    public String toString() {
        return "TrackedRace for "+getRace();
    }

    @Override
    public List<GPSFixMoving> approximate(Competitor competitor, Distance maxDistance, TimePoint from, TimePoint to) {
        DouglasPeucker<Competitor, GPSFixMoving> douglasPeucker = new DouglasPeucker<Competitor, GPSFixMoving>(
                getTrack(competitor));
        return douglasPeucker.approximate(maxDistance, from, to);
    }
    
    /**
     * Caches results in {@link #maneuverCache}. The cache is {@link #clearAllCaches() invalidated} by any
     * {@link #updated(TimePoint) update}. Therefore, it is mainly useful for completed races. The cache tries to grow
     * the time interval for which the maneuvers of a competitor have been computed. If <code>from</code> and
     * <code>to</code> are within an interval already cached, the interval requested is
     * {@link #extractInterval(TimePoint, TimePoint, List) extracted} from the maneuver list cached. Otherwise,
     * the cached interval (empty in case no maneuvers were cached for <code>competitor</code> yet) is extended to
     * include <code>from..to</code> by computing and caching the maneuvers for the new, extended interval. From the resulting
     * extended maneuver list the interval requested is then {@link #extractInterval(TimePoint, TimePoint, List) extracted}.
     */
    @Override
    public List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to) throws NoWindException {
        List<Maneuver> result;
        Triple<TimePoint, TimePoint, List<Maneuver>> fromToAndManeuvers;
        synchronized (maneuverCache) {
            fromToAndManeuvers = maneuverCache.get(competitor);
        }
        if (fromToAndManeuvers != null && from.compareTo(fromToAndManeuvers.getA()) >= 0
                && to.compareTo(fromToAndManeuvers.getB()) <= 0) {
            // cached maneuver list contains interval requested
            result = extractInterval(from, to, fromToAndManeuvers.getC());
        } else {
            TimePoint extendedFrom = fromToAndManeuvers == null ? from
                    : from.compareTo(fromToAndManeuvers.getA()) <= 0 ? from : fromToAndManeuvers.getA();
            TimePoint extendedTo = fromToAndManeuvers == null ? to : to.compareTo(fromToAndManeuvers.getB()) >= 0 ? to
                    : fromToAndManeuvers.getB();
            List<Maneuver> extendedResultForCache = detectManeuvers(competitor,
                    approximate(competitor, getRace().getBoatClass().getMaximumDistanceForCourseApproximation(),
                            extendedFrom, extendedTo));
            result = extractInterval(from, to, extendedResultForCache);
            synchronized (maneuverCache) {
                maneuverCache.put(competitor, new Triple<TimePoint, TimePoint, List<Maneuver>>(extendedFrom,
                        extendedTo, extendedResultForCache));
            }
        }
        return result;
    }

    private <T extends Timed> List<T> extractInterval(TimePoint from, TimePoint to, List<T> listOfTimed) {
        List<T> result;
        result = new LinkedList<T>();
        for (T timed : listOfTimed) {
            if (timed.getTimePoint().compareTo(from) >= 0 && timed.getTimePoint().compareTo(to) <= 0) {
                result.add(timed);
            }
        }
        return result;
    }
    
    /**
     * Tries to detect maneuvers on the <code>competitor</code>'s track based on a number of approximating fixes. The
     * fixes contain bearing information, but this is not the bearing leading to the next approximation fix but the
     * bearing the boat had at the time of the approximating fix which is taken from the original track.
     * 
     * The time period assumed for a maneuver duration is taken from the
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() boat class}. If no maneuver is detected, an empty
     * list is returned. Maneuvers can only be expected to be detected if at least three fixes are provided in
     * <code>approximatedFixesToAnalyze</code>. For the inner approximating fixes (all except the first and the last
     * approximating fix), their course changes according to the approximated path (and not the underlying actual
     * tracked fixes) are computed. Subsequent course changes to the same direction are then grouped. Those in closer
     * timely distance than {@link #getApproximateManeuverDurationInMilliseconds()} (including single course changes
     * that have no surrounding other course changes to group) are grouped into one {@link Maneuver}.
     * 
     * @return an empty list if no maneuver is detected for <code>competitor</code> between <code>from</code> and
     *         <code>to</code>, or else the list of maneuvers detected.
     */
    private List<Maneuver> detectManeuvers(Competitor competitor, List<GPSFixMoving> approximatingFixesToAnalyze) throws NoWindException {
        List<Maneuver> result = new ArrayList<Maneuver>();
        if (approximatingFixesToAnalyze.size() > 2) {
            List<Pair<GPSFixMoving, CourseChange>> courseChangeSequenceInSameDirection = new ArrayList<Pair<GPSFixMoving, CourseChange>>();
            Iterator<GPSFixMoving> approximationPointsIter = approximatingFixesToAnalyze.iterator();
            GPSFixMoving previous = approximationPointsIter.next();
            GPSFixMoving current = approximationPointsIter.next();
            // the bearings in these variables are between approximation points
            SpeedWithBearing speedWithBearingOnApproximationFromPreviousToCurrent = previous.getSpeedAndBearingRequiredToReach(current);
            SpeedWithBearing speedWithBearingOnApproximationAtBeginningOfUnidirectionalCourseChanges = speedWithBearingOnApproximationFromPreviousToCurrent;
            SpeedWithBearing speedWithBearingOnApproximationFromCurrentToNext; // will certainly be assigned because iter's collection's size > 2
            do {
                GPSFixMoving next = approximationPointsIter.next();
                speedWithBearingOnApproximationFromCurrentToNext = current.getSpeedAndBearingRequiredToReach(next);
                // compute course change on "approximation track"
                CourseChange courseChange = speedWithBearingOnApproximationFromPreviousToCurrent
                        .getCourseChangeRequiredToReach(speedWithBearingOnApproximationFromCurrentToNext);
                Pair<GPSFixMoving, CourseChange> courseChangeAtFix = new Pair<GPSFixMoving, CourseChange>(current, courseChange);
                if (!courseChangeSequenceInSameDirection.isEmpty() &&
                        Math.signum(courseChangeSequenceInSameDirection.get(0).getB().getCourseChangeInDegrees()) !=
                        Math.signum(courseChange.getCourseChangeInDegrees())) {
                    // course change in different direction; cluster the course changes in same direction so far, then start new list
                    List<Maneuver> maneuvers = groupChangesInSameDirectionIntoManeuvers(competitor,
                            speedWithBearingOnApproximationAtBeginningOfUnidirectionalCourseChanges, courseChangeSequenceInSameDirection);
                    result.addAll(maneuvers);
                    courseChangeSequenceInSameDirection.clear();
                    speedWithBearingOnApproximationAtBeginningOfUnidirectionalCourseChanges = speedWithBearingOnApproximationFromPreviousToCurrent;
                }
                courseChangeSequenceInSameDirection.add(courseChangeAtFix);
                previous = current;
                current = next;
                speedWithBearingOnApproximationFromPreviousToCurrent = speedWithBearingOnApproximationFromCurrentToNext;
            } while (approximationPointsIter.hasNext());
            if (!courseChangeSequenceInSameDirection.isEmpty()) {
                result.addAll(groupChangesInSameDirectionIntoManeuvers(competitor, speedWithBearingOnApproximationAtBeginningOfUnidirectionalCourseChanges,
                        courseChangeSequenceInSameDirection));
            }
        }
        return result;
    }

    /**
     * Groups the {@link CourseChange} sequence into groups where the times of the fixes at which the course changes
     * took place are no further apart than {@link #getApproximateManeuverDurationInMilliseconds()} milliseconds or
     * where the distances of those course changes are less than two hull lengths apart. For those, a single
     * {@link Maneuver} object is created and added to the resulting list. The maneuver sums up the direction changes of
     * the individual {@link CourseChange} objects. This can result in direction changes of more than 180 degrees in one
     * direction which may, e.g., represent a penalty circle or a mark rounding maneuver. As the maneuver's time point,
     * the average time point of the course changes that went into the maneuver construction is used.
     * <p>
     * 
     * @param speedWithBearingOnApproximationAtBeginning
     *            the speed/bearing before the first approximating fix passed in
     *            <code>courseChangeSequenceInSameDirection</code>
     * @param courseChangeSequenceInSameDirection
     *            all expected to have equal {@link CourseChange#to()} values
     * 
     * @return a non-<code>null</code> list
     */
    private List<Maneuver> groupChangesInSameDirectionIntoManeuvers(Competitor competitor,
            SpeedWithBearing speedWithBearingOnApproximationAtBeginning,
            List<Pair<GPSFixMoving, CourseChange>> courseChangeSequenceInSameDirection) throws NoWindException {
        List<Maneuver> result = new ArrayList<Maneuver>();
        List<Pair<GPSFixMoving, CourseChange>> group = new ArrayList<Pair<GPSFixMoving, CourseChange>>();
        if (!courseChangeSequenceInSameDirection.isEmpty()) {
            Distance twoHullLengths = competitor.getBoat().getBoatClass().getHullLength().scale(2);
            SpeedWithBearing beforeGroupOnApproximation = speedWithBearingOnApproximationAtBeginning; // speed/bearing before group
            SpeedWithBearing beforeCurrentCourseChangeOnApproximation = beforeGroupOnApproximation; // speed/bearing before current course change
            Iterator<Pair<GPSFixMoving, CourseChange>> iter = courseChangeSequenceInSameDirection.iterator();
            double totalCourseChangeInDegrees = 0.0;
            long totalMilliseconds = 0l;
            SpeedWithBearing afterCurrentCourseChange = null; // sure to be set because iter's collection is not empty
            // and the first use requires group not to be empty which can only happen after the first group.add
            do {
                Pair<GPSFixMoving, CourseChange> currentFixAndCourseChange = iter.next();
                if (!group.isEmpty()
                        && currentFixAndCourseChange.getA().getTimePoint().asMillis()
                                - group.get(group.size() - 1).getA().getTimePoint().asMillis() > getApproximateManeuverDurationInMilliseconds()
                        && currentFixAndCourseChange.getA().getPosition().getDistance(
                                group.get(group.size() - 1).getA().getPosition()).compareTo(twoHullLengths) > 0) {
                    // if next is more then approximate maneuver duration later or further apart than two hull lengths,
                    // turn the current group into a maneuver and add to result
                    Maneuver maneuver = createManeuverFromGroupOfCourseChanges(competitor, beforeGroupOnApproximation,
                            group, afterCurrentCourseChange, totalCourseChangeInDegrees, totalMilliseconds);
                    result.add(maneuver);
                    group.clear();
                    totalCourseChangeInDegrees = 0.0;
                    totalMilliseconds = 0l;
                    beforeGroupOnApproximation = beforeCurrentCourseChangeOnApproximation;
                }
                afterCurrentCourseChange = beforeCurrentCourseChangeOnApproximation.applyCourseChange(currentFixAndCourseChange.getB());
                totalMilliseconds += currentFixAndCourseChange.getA().getTimePoint().asMillis();
                totalCourseChangeInDegrees += currentFixAndCourseChange.getB().getCourseChangeInDegrees();
                group.add(currentFixAndCourseChange);
                beforeCurrentCourseChangeOnApproximation = afterCurrentCourseChange; // speed/bearing after course change
            } while (iter.hasNext());
            if (!group.isEmpty()) {
                result.add(createManeuverFromGroupOfCourseChanges(competitor, beforeGroupOnApproximation,
                            group, afterCurrentCourseChange, totalCourseChangeInDegrees, totalMilliseconds));
            }
        }
        return result;
    }

    private Maneuver createManeuverFromGroupOfCourseChanges(Competitor competitor,
            SpeedWithBearing speedWithBearingOnApproximationAtBeginning, List<Pair<GPSFixMoving, CourseChange>> group,
            SpeedWithBearing speedWithBearingOnApproximationAtEnd, double totalCourseChangeInDegrees, long totalMilliseconds)
            throws NoWindException {
        TimePoint maneuverTimePoint = new MillisecondsTimePoint(totalMilliseconds/group.size());
        Position maneuverPosition = getTrack(competitor).getEstimatedPosition(maneuverTimePoint, /* extrapolate */ false);
        MillisecondsTimePoint timePointBeforeManeuver = new MillisecondsTimePoint(group.get(0).getA().getTimePoint()
                .asMillis() - getApproximateManeuverDurationInMilliseconds()/2);
        MillisecondsTimePoint timePointAfterManeuver = new MillisecondsTimePoint(group.get(group.size() - 1).getA()
                .getTimePoint().asMillis() + getApproximateManeuverDurationInMilliseconds()/2);
        Tack tackBeforeManeuver = getTack(maneuverPosition, timePointBeforeManeuver, speedWithBearingOnApproximationAtBeginning.getBearing());
        Tack tackAfterManeuver = getTack(maneuverPosition, timePointAfterManeuver, speedWithBearingOnApproximationAtEnd.getBearing());
        // the TrackedLegOfCompetitor variables may be null, e.g., in case the time points are before or after the race
        TrackedLegOfCompetitor legBeforeManeuver = getTrackedLeg(competitor, timePointBeforeManeuver);
        TrackedLegOfCompetitor legAfterManeuver = getTrackedLeg(competitor, timePointAfterManeuver);
        ManeuverType maneuverType;
        if (Math.abs(totalCourseChangeInDegrees) > PENALTY_CIRCLE_DEGREES_THRESHOLD) {
            maneuverType = ManeuverType.PENALTY_CIRCLE;
        } else if (legBeforeManeuver != legAfterManeuver &&
                // a maneuver at the start line is not to be considered a MARK_PASSING maneuver; show a tack as a tack
                legAfterManeuver != null && legAfterManeuver.getLeg().getFrom() != getRace().getCourse().getFirstWaypoint()) {
            maneuverType = ManeuverType.MARK_PASSING;
        } else {
            if (tackBeforeManeuver != tackAfterManeuver) {
                LegType legType = legBeforeManeuver!=null ?
                        getTrackedLeg(legBeforeManeuver.getLeg()).getLegType(timePointBeforeManeuver) :
                            legAfterManeuver!=null ? getTrackedLeg(legAfterManeuver.getLeg()).getLegType(timePointAfterManeuver) : null;
                if (legType != null) {
                    // tack or jibe
                    switch (legType) {
                    case UPWIND:
                        maneuverType = ManeuverType.TACK;
                        break;
                    case DOWNWIND:
                        maneuverType = ManeuverType.JIBE;
                        break;
                    default:
                        maneuverType = ManeuverType.UNKNOWN;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Unknown maneuver for "
                                    + competitor + " at " + maneuverTimePoint
                                    + (legBeforeManeuver != null ? " on reaching leg " + legBeforeManeuver.getLeg()
                                            : " before start"));
                        }
                        break;
                    }
                } else {
                    maneuverType = ManeuverType.UNKNOWN;
                    logger.fine("Can't determine leg type because tracked legs for competitor "+competitor+
                            " cannot be determined for time points "+timePointBeforeManeuver+" and "+
                            timePointAfterManeuver);
                }
            } else {
                // heading up or bearing away
                Wind wind = getWind(maneuverPosition, maneuverTimePoint);
                Bearing windBearing = wind.getBearing();
                Bearing toWindBeforeManeuver = windBearing.getDifferenceTo(speedWithBearingOnApproximationAtBeginning.getBearing());
                Bearing toWindAfterManeuver = windBearing.getDifferenceTo(speedWithBearingOnApproximationAtEnd.getBearing());
                maneuverType = Math.abs(toWindBeforeManeuver.getDegrees()) < Math.abs(toWindAfterManeuver.getDegrees()) ?
                        ManeuverType.HEAD_UP : ManeuverType.BEAR_AWAY;
            }
        }
        Maneuver maneuver = new ManeuverImpl(maneuverType, tackAfterManeuver, maneuverPosition, maneuverTimePoint, speedWithBearingOnApproximationAtBeginning,
                speedWithBearingOnApproximationAtEnd, totalCourseChangeInDegrees);
        return maneuver;
    }

    /**
     * Fetches the boat class-specific parameter
     */
    private double getManeuverDegreeAngleThreshold() {
        return getRace().getBoatClass().getManeuverDegreeAngleThreshold();
    }

    private double getMinimumAngleBetweenDifferentTacksDownwind() {
        return getRace().getBoatClass().getMinimumAngleBetweenDifferentTacksDownwind();
    }

    private double getMinimumAngleBetweenDifferentTacksUpwind() {
        return getRace().getBoatClass().getMinimumAngleBetweenDifferentTacksUpwind();
    }
    
    private long getApproximateManeuverDurationInMilliseconds() {
        return getRace().getBoatClass().getApproximateManeuverDurationInMilliseconds();
    }

    private class StartToNextMarkCacheInvalidationListener implements GPSTrackListener<Buoy> {
        private final GPSFixTrack<Buoy, GPSFix> listeningTo;
        
        public StartToNextMarkCacheInvalidationListener(GPSFixTrack<Buoy, GPSFix> listeningTo) {
            this.listeningTo = listeningTo;
        }
        
        public void stopListening() {
            listeningTo.removeListener(this);
        }
        
        @Override
        public void gpsFixReceived(GPSFix fix, Buoy buoy) {
            directionFromStartToNextMarkCache = null;
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        }
    }
    
    @Override
    public RacePlaceOrder getPlaceOrder() {
        RacePlaceOrder order = null;
        Placemark startBest = null;
        Placemark finishBest = null;
        
        //Get start postition
        Waypoint start = getRace().getCourse().getFirstWaypoint();
        Iterable<MarkPassing> startPassings = getMarkPassingsInOrder(start);
        MarkPassing startPassing = startPassings.iterator().next();
        Position startPosition = getApproximatePosition(start, startPassing.getTimePoint());
        
        try {
            //Get distance to nearest placemark and calculate the search radius
            Placemark startNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(startPosition);
            Distance startNearestDistance = startNearest.distanceFrom(startPosition);
            double startRadius = startNearestDistance.getKilometers() * GEONAMES_RADIUS_CACLCULATION_FACTOR;
            
            //Get the estimated best start place
            startBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(startPosition, startRadius,
                    new Placemark.ByPopulationDistanceRatio(startPosition));
        } catch (IOException e) {
            logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
        } catch (ParseException e) {
            logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
        }

        //Get finish position
        Waypoint finish = getRace().getCourse().getLastWaypoint();
        Iterable<MarkPassing> finishPassings = getMarkPassingsInOrder(finish);
        Iterator<MarkPassing> finishPassingsIterator = finishPassings.iterator();
        MarkPassing finishPassing = null;
        while (finishPassingsIterator.hasNext()) {
            finishPassing = (MarkPassing) finishPassingsIterator.next();
        }
        Position finishPosition = getApproximatePosition(finish, finishPassing.getTimePoint());
        
        if (startPosition.getDistance(finishPosition).getKilometers() > ReverseGeocoder.POSITION_CACHE_DISTANCE_LIMIT_IN_KM) {
            try {
                // Get distance to nearest placemark and calculate the search radius
                Placemark finishNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(finishPosition);
                Distance finishNearestDistance = finishNearest.distanceFrom(finishPosition);
                double finishRadius = finishNearestDistance.getKilometers() * GEONAMES_RADIUS_CACLCULATION_FACTOR;

                // Get the estimated best finish place
                finishBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(finishPosition, finishRadius,
                        new Placemark.ByPopulationDistanceRatio(finishPosition));
            } catch (IOException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            } catch (ParseException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            }
        }
        
        if (startBest != null) {
            if (finishBest != null) {
                order = new RacePlaceOrderImpl(startBest, finishBest);
            } else {
                order = new RacePlaceOrderImpl(startBest, startBest);
            }
        }
        
        return order;
    }
    
}
