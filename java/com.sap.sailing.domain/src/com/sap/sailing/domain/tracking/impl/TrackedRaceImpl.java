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

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DouglasPeucker;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Maneuver.Type;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindError;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLeg.LegType;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.util.Util;
import com.sap.sailing.util.Util.Pair;

public abstract class TrackedRaceImpl implements TrackedRace, CourseListener {
    private static final Logger logger = Logger.getLogger(TrackedRaceImpl.class.getName());

    private static final double PENALTY_CIRCLE_DEGREES_THRESHOLD = 320;

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

    private boolean warnedOfUsingWindFromAlternativeWindSource;

    private boolean warnedOfNoWindFromSelectedSource;

    private boolean warnedOfUsingLegDirectionAsWindEstimation;
    
    public TrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.updateCount = 0;
        this.race = race;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
        this.buoyTracks = new HashMap<Buoy, GPSFixTrack<Buoy, GPSFix>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                getOrCreateTrack(buoy);
            }
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
        currentWindSource = WindSource.EXPEDITION;
        competitorRankings = new HashMap<TimePoint, List<Competitor>>();
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
                result = new DynamicGPSFixTrackImpl<Buoy>(buoy, millisecondsOverWhichToAverageSpeed);
                buoyTracks.put(buoy, result);
            }
            return result;
        }
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
    public Wind getWind(Position p, TimePoint at) {
        Wind result = getWindTrack(currentWindSource).getEstimatedWind(p, at);
        if (result == null) {
            if (!warnedOfNoWindFromSelectedSource) {
                logger.warning("Couldn't find any wind information for race "+getRace()+" from currently selected source "+currentWindSource+
                        ". Future warnings of this type will be suppressed for this race.");
                warnedOfNoWindFromSelectedSource = true;
            }
            for (WindSource alternativeWindSource : WindSource.values()) {
                if (alternativeWindSource != currentWindSource) {
                    result = getWindTrack(alternativeWindSource).getEstimatedWind(p, at);
                    if (result != null) {
                        if (!warnedOfUsingWindFromAlternativeWindSource) {
                            logger.warning("Found wind settings in alternative wind source "+alternativeWindSource+
                                    " which will be used as a fallback. Future warnings of this type will be suppressed for this race.");
                            warnedOfUsingWindFromAlternativeWindSource = true;
                        }
                        break;
                    }
                }
            }
            if (result == null) {
                if (!warnedOfUsingLegDirectionAsWindEstimation) {
                    logger.warning("Found no other wind settings either; using starting leg direction as guess for wind direction. Force assumed as 1 knot."+
                            " Future warnings of this type will be suppressed for this race.");
                    warnedOfUsingLegDirectionAsWindEstimation = true;
                }
                result = getDirectionFromStartToNextMark(at);
            }
        }
        return result;
    }

    @Override
    public Wind getDirectionFromStartToNextMark(TimePoint at) {
        Wind result;
        Leg firstLeg = getRace().getCourse().getLegs().iterator().next();
        Position firstLegEnd = getApproximatePosition(firstLeg.getTo(), at);
        Position firstLegStart = getApproximatePosition(firstLeg.getFrom(), at);
        result = new WindImpl(firstLegStart, at, new KnotSpeedWithBearingImpl(1.0, firstLegEnd.getBearingGreatCircle(firstLegStart)));
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
        if (toRemove == null && !trackedLegs.isEmpty()) {
            // last waypoint removed
            toRemove = last;
        }
        if (toRemove != null) {
            trackedLegs.remove(toRemove);
            updated(/* time point*/ null);
        }
    }
    
    @Override
    public TrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public Wind getEstimatedWindDirection(Position position, TimePoint timePoint) throws NoWindException {
        Map<LegType, BearingCluster> bearings = new HashMap<TrackedLeg.LegType, BearingCluster>();
        for (LegType legType : LegType.values()) {
            bearings.put(legType, new BearingCluster());
        }
        for (Competitor competitor : getRace().getCompetitors()) {
            TrackedLegOfCompetitor leg = getTrackedLeg(competitor, timePoint);
            if (leg != null) {
                TrackedLeg trackedLeg = getTrackedLeg(leg.getLeg());
                LegType legType = trackedLeg.getLegType(timePoint);
                if (legType != LegType.REACHING) {
                    GPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
                    if (!track.hasDirectionChange(timePoint, getManeuverDegreeAngleThreshold())) {
                        Bearing bearing = track.getEstimatedSpeed(timePoint).getBearing();
                        BearingCluster bearingClusters = bearings.get(legType);
                        bearingClusters.add(bearing);
                    }
                }
            }
        }
        Bearing upwindAverage = null;
        BearingCluster[] bearingClustersUpwind = bearings.get(LegType.UPWIND).splitInTwo(getMinimumAngleBetweenDifferentTacksUpwind());
        if (!bearingClustersUpwind[0].isEmpty() && !bearingClustersUpwind[1].isEmpty()) {
            upwindAverage = bearingClustersUpwind[0].getAverage().middle(bearingClustersUpwind[1].getAverage());
        }
        Bearing downwindAverage = null;
        BearingCluster[] bearingClustersDownwind = bearings.get(LegType.DOWNWIND).splitInTwo(getMinimumAngleBetweenDifferentTacksDownwind());
        if (!bearingClustersDownwind[0].isEmpty() && !bearingClustersDownwind[1].isEmpty()) {
            downwindAverage = bearingClustersDownwind[0].getAverage().middle(bearingClustersDownwind[1].getAverage());
        }
        double bearingDeg;
        if (upwindAverage == null) {
            if (downwindAverage == null) {
                throw new NoWindException(
                        "Can't determine estimated wind direction because no two distinct direction clusters found upwind nor downwind");
            } else {
                bearingDeg = downwindAverage.getDegrees();
            }
        } else {
            if (downwindAverage == null) {
                bearingDeg = upwindAverage.reverse().getDegrees();
            } else {
                bearingDeg = (downwindAverage.getDegrees() + upwindAverage.reverse().getDegrees())/2.0;
            }
        }
        return new WindImpl(null, timePoint,
                new KnotSpeedWithBearingImpl(/* speedInKnots */ 1, new DegreeBearingImpl(bearingDeg)));
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
        DouglasPeucker<Competitor, GPSFixMoving> douglasPeucker = new DouglasPeucker<Competitor, GPSFixMoving>(getTrack(competitor)); 
        return douglasPeucker.approximate(maxDistance, from, to);
    }
    
    @Override
    public List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to) throws NoWindException {
        return detectManeuvers(competitor, approximate(competitor, getRace().getBoatClass().getMaximumDistanceForCourseApproximation(), from, to));
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
                CourseChange courseChange = speedWithBearingOnApproximationFromPreviousToCurrent.getCourseChangeRequiredToReach(speedWithBearingOnApproximationFromCurrentToNext);
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
     * took place are no further apart than {@link #getApproximateManeuverDurationInMilliseconds()} milliseconds. For
     * those, a single {@link Maneuver} object is created and added to the resulting list. The maneuver sums up the
     * direction changes of the individual {@link CourseChange} objects. This can result in direction changes of more
     * than 180 degrees in one direction which may, e.g., represent a penalty circle or a mark rounding maneuver. As the
     * maneuver's time point, the average time point of the course changes that went into the maneuver construction is
     * used.<p>
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
                        && currentFixAndCourseChange.getA().getTimePoint().asMillis() - group.get(group.size() - 1).getA().getTimePoint().asMillis() >
                        getApproximateManeuverDurationInMilliseconds()) {
                    // if next is more then approximate maneuver duration later, turn the current group into a maneuver and add to result
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
        Maneuver.Type maneuverType;
        if (totalCourseChangeInDegrees > PENALTY_CIRCLE_DEGREES_THRESHOLD) {
            maneuverType = Type.PENALTY_CIRCLE;
        } else if (legBeforeManeuver != legAfterManeuver &&
                // a maneuver at the start line is not to be considered a MARK_PASSING maneuver; show a tack as a tack
                legAfterManeuver != null && legAfterManeuver.getLeg().getFrom() != getRace().getCourse().getFirstWaypoint()) {
            maneuverType = Type.MARK_PASSING;
        } else {
            if (tackBeforeManeuver != tackAfterManeuver) {
                LegType legType = legBeforeManeuver!=null ?
                        getTrackedLeg(legBeforeManeuver.getLeg()).getLegType(timePointBeforeManeuver) :
                            legAfterManeuver!=null ? getTrackedLeg(legAfterManeuver.getLeg()).getLegType(timePointAfterManeuver) : null;
                if (legType != null) {
                    // tack or jibe
                    switch (legType) {
                    case UPWIND:
                        maneuverType = Type.TACK;
                        break;
                    case DOWNWIND:
                        maneuverType = Type.JIBE;
                        break;
                    default:
                        maneuverType = Type.UNKNOWN;
                        logger.fine("Unknown maneuver for " + competitor + " at " + maneuverTimePoint
                                + (legBeforeManeuver != null ? " on reaching leg " + legBeforeManeuver.getLeg() : " before start"));
                        break;
                    }
                } else {
                    maneuverType = Type.UNKNOWN;
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
                        Type.HEAD_UP : Type.BEAR_AWAY;
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

}
