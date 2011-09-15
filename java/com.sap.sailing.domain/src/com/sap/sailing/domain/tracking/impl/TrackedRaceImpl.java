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
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
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

public abstract class TrackedRaceImpl implements TrackedRace, CourseListener {
    /**
     * If the averaged courses over ground differ by at least this degree angle, a maneuver will
     * be assumed. Note that this should be much less than the tack angle because averaging may
     * span across the actual maneuver.
     */
    private static final double MANEUVER_DEGREE_ANGLE_THRESHOLD = /* minimumDegreeDifference */ 30.;

    private static final Logger logger = Logger.getLogger(TrackedRaceImpl.class.getName());
    
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
    
    public TrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.updateCount = 0;
        this.race = race;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
        this.buoyTracks = new HashMap<Buoy, GPSFixTrack<Buoy, GPSFix>>();
        for (Waypoint waypoint : race.getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                getTrack(buoy);
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
        return getStart().compareTo(at) <= 0;
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
    public GPSFixTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
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
            Position nextPos = getTrack(buoy).getEstimatedPosition(timePoint, /* extrapolate */ false);
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
            if (result == null) {
                logger.warning("Found no other wind settings either; using starting leg direction as guess for wind direction. Force assumed as 1 knot.");
                Leg firstLeg = getRace().getCourse().getLegs().iterator().next();
                Position firstLegEnd = getApproximatePosition(firstLeg.getTo(), at);
                Position firstEndStart = getApproximatePosition(firstLeg.getFrom(), at);
                result = new WindImpl(p, at, new KnotSpeedWithBearingImpl(1.0, firstLegEnd.getBearingGreatCircle(firstEndStart)));
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
            getTrack(buoy);
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
    
    @Override
    public TrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public Wind getEstimatedWindDirection(Position position, TimePoint timePoint) throws NoWindException {
        Map<LegType, BearingCluster[]> bearings = new HashMap<TrackedLeg.LegType, BearingCluster[]>();
        for (LegType legType : LegType.values()) {
            bearings.put(legType, new BearingCluster[] { new BearingCluster(), new BearingCluster() });
        }
        for (Competitor competitor : getRace().getCompetitors()) {
            TrackedLegOfCompetitor leg = getTrackedLeg(competitor, timePoint);
            if (leg != null) {
                TrackedLeg trackedLeg = getTrackedLeg(leg.getLeg());
                LegType legType = trackedLeg.getLegType(timePoint);
                if (legType != LegType.REACHING) {
                    GPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
                    if (!track.hasDirectionChange(timePoint, MANEUVER_DEGREE_ANGLE_THRESHOLD)) {
                        Bearing bearing = track.getEstimatedSpeed(timePoint).getBearing();
                        BearingCluster[] bearingClusters = bearings.get(legType);
                        // add to the cluster "closest" to the fix
                        // FIXME this would cluster the first two bearings even if on the same side; add all to the same cluster and split afterwards
                        if (bearingClusters[0].getDifferenceFromAverage(bearing) <= bearingClusters[1].getDifferenceFromAverage(bearing)) {
                            bearingClusters[0].add(bearing);
                        } else {
                            bearingClusters[1].add(bearing);
                        }
                    }
                }
            }
        }
        Bearing upwindAverage = null;
        if (!bearings.get(LegType.UPWIND)[0].isEmpty() && !bearings.get(LegType.UPWIND)[1].isEmpty()) {
            upwindAverage = new DegreeBearingImpl((bearings.get(LegType.UPWIND)[0].getAverage().getDegrees() + bearings
                .get(LegType.UPWIND)[1].getAverage().getDegrees()) / 2.0);
        }
        Bearing downwindAverage = null;
        if (!bearings.get(LegType.DOWNWIND)[0].isEmpty() && !bearings.get(LegType.DOWNWIND)[1].isEmpty()) {
            downwindAverage = new DegreeBearingImpl((bearings.get(LegType.DOWNWIND)[0].getAverage().getDegrees() + bearings
                .get(LegType.DOWNWIND)[1].getAverage().getDegrees()) / 2.0);
        }
        double bearingDeg;
        if (upwindAverage == null) {
            if (downwindAverage == null) {
                throw new NoWindException(
                        "Can't determine estimated wind direction because no two distinct direction clusted found upwind nor downwind");
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
    public Tack getTack(Competitor competitor, TimePoint timePoint) throws NoWindException {
        Bearing wind = getWind(getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolate */ false), timePoint).getBearing();
        Bearing boat = getTrack(competitor).getEstimatedSpeed(timePoint).getBearing();
        Bearing difference = wind.getDifferenceTo(boat);
        return difference.getDegrees() <= 0 ? Tack.STARBOARD : Tack.PORT;
    }

    @Override
    public String toString() {
        return "TrackedRace for "+getRace();
    }
}
