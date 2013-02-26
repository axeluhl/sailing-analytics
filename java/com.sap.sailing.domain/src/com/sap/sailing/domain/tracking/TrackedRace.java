package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DouglasPeucker;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.Util.Pair;

/**
 * Live tracking data of a single race. The race follows a defined {@link Course} with a sequence of {@link Leg}s. The
 * course may change over time as the race committee decides to change it. Therefore, a {@link TrackedRace} instance
 * {@link Course#addCourseListener(com.sap.sailing.domain.base.CourseListener) observes} the race {@link Course} for
 * such changes. The tracking information of a leg can be requested either for all competitors (see
 * {@link #getTrackedLegs()} and {@link #getTrackedLeg(Leg)}) or for a single competitor (see
 * {@link #getTrackedLeg(Competitor, Leg)}).
 * <p>
 * 
 * The overall race standings can be requested in terms of a competitor's ranking. More detailed information about what
 * happens / happened within a leg is available from {@link TrackedLeg} and {@link TrackedLegOfCompetitor}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface TrackedRace extends Serializable {
    final long MAX_TIME_BETWEEN_START_AND_FIRST_MARK_PASSING_IN_MILLISECONDS = 30000;

    final long DEFAULT_LIVE_DELAY_IN_MILLISECONDS = 5000;

    RaceDefinition getRace();

    RegattaAndRaceIdentifier getRaceIdentifier();

    /**
     * Computes the estimated start time for this race (not to be confused with the {@link #getStartOfTracking()} time
     * point which is expected to be before the race start time). When there are no {@link MarkPassing}s for the first
     * mark, <code>null</code> is returned. If there are mark passings for the first mark and the start time is less
     * than {@link #MAX_TIME_BETWEEN_START_AND_FIRST_MARK_PASSING_IN_MILLISECONDS} before the first mark passing for the
     * first mark. Otherwise, the first mark passing for the first mark minus
     * {@link #MAX_TIME_BETWEEN_START_AND_FIRST_MARK_PASSING_IN_MILLISECONDS} is returned as the race start time.
     * <p>
     * 
     * If no start time can be determined this way, <code>null</code> is returned.
     */
    TimePoint getStartOfRace();

    /**
     * Determine the race end time is tricky. Boats may sink, stop, not finish, although they started the race. We
     * therefore cannot wait for all boats to reach the finish line. The following rules are used to calculate the
     * endOfRace:
     * <ol>
     * <li>Returns <code>null</code> if no boat passed the finish line</li>
     * <li>Returns time of the last mark passing recorded for the finish line</li>
     * <li>TODO: Returns the time of the first passing of the finish line + the target window (defined in the
     * competition rules) if a target window has been defined for the race</li>
     * </ol>
     */
    TimePoint getEndOfRace();

    /**
     * Returns a list of the first and last mark passing times of all course waypoints. Callers wanting to iterate over
     * the result must <code>synchronize</code> on the result.
     */
    Iterable<Pair<Waypoint, Pair<TimePoint, TimePoint>>> getMarkPassingsTimes();

    /**
     * Shorthand for <code>{@link #getStart()}.{@link TimePoint#compareTo(TimePoint) compareTo(at)} &lt;= 0</code>
     */
    boolean hasStarted(TimePoint at);

    /**
     * Clients can safely iterate over the iterable returned because it's a non-live copy of the tracked legs of this
     * tracked race. This implies that should an update to the underlying list of waypoints in this race's
     * {@link Course} take place after this method has returned, then this won't be reflected in the result returned.
     * Callers should obtain the {@link Course#lockForRead() course's read lock} while using the result of this call if
     * they want to ensure that no course update is applied concurrently.
     */
    Iterable<TrackedLeg> getTrackedLegs();

    TrackedLeg getTrackedLeg(Leg leg);

    /**
     * Tracking information about the leg <code>competitor</code> is on at <code>timePoint</code>, or <code>null</code>
     * if the competitor hasn't started any leg yet at <code>timePoint</code> or has already finished the race.
     */
    TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint);

    /**
     * Tells which leg the leader at <code>timePoint</code> is on
     */
    TrackedLeg getCurrentLeg(TimePoint timePoint);

    /**
     * Precondition: waypoint must still be part of {@link #getRace()}.{@link RaceDefinition#getCourse() getCourse()}.
     */
    TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg);

    /**
     * Precondition: waypoint must still be part of {@link #getRace()}.{@link RaceDefinition#getCourse() getCourse()}.
     */
    TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    GPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);

    /**
     * Tells the leg on which the <code>competitor</code> was at time <code>at</code>. If the competitor hasn't passed
     * the start waypoint yet, <code>null</code> is returned because the competitor was not yet on any leg at that point
     * in time. If the time point happens to be after the last fix received from that competitor, the last known leg for
     * that competitor is returned. If the time point is after the competitor's mark passing for the finish line,
     * <code>null</code> is returned. For all legs except the last, if the time point equals a mark passing time point
     * of the leg's starting waypoint, that leg is returned. For the time point of the mark passing for the finish line,
     * the last leg is returned.
     */
    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at);

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg);

    /**
     * @return a sequential number counting the updates that occurred to this tracked race. Callers may use this to ask
     *         for updates newer than such a sequence number.
     */
    long getUpdateCount();

    int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint);

    /**
     * Computes the rank of the competitor in this race for the current time.
     */
    int getRank(Competitor competitor) throws NoWindException;

    /**
     * Computes the rank of <code>competitor</code> in this race. A competitor is ahead of all competitors that are one
     * or more legs behind. Within the same leg, the rank is determined by the windward distance to go and therefore
     * depends on the assumptions of the wind direction for the given <code>timePoint</code>. If the race hasn't
     * {@link #hasStarted(TimePoint) started} yet, the result is undefined.
     * 
     * @return <code>0</code> in case the competitor hasn't participated in the race; a rank starting with
     *         <code>1</code> where rank <code>1</code> identifies the leader otherwise
     */
    int getRank(Competitor competitor, TimePoint timePoint) throws NoWindException;

    /**
     * For a competitor, computes the distance (TODO not yet clear whether over ground or projected onto wind direction)
     * into the race <code>secondsIntoTheRace</code> after the race {@link TrackedRace#getStart() started}.
     */
    Distance getStartAdvantage(Competitor competitor, double secondsIntoTheRace);

    /**
     * For the given waypoint lists the {@link MarkPassing} events that describe which competitor passed the waypoint at
     * which point in time. This can, e.g., be used to sort those competitors who already finished a leg within the leg
     * that ends with <code>waypoint</code>. The remaining competitors need to be ordered by the advantage line-related
     * distance to the waypoint.
     * 
     * @return the iterable sequence of {@link MarkPassing}s as described above. If the caller wants to iterate on the
     *         resulting collection, the caller needs to invoke {@link #lockForRead(Iterable)} with the collection
     *         returned as parameter because insertions into the competitor's mark passing collection will obtain the
     *         corresponding write lock.
     */
    Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint);

    /**
     * Obtains the {@link MarkPassing} for <code>competitor</code> passing <code>waypoint</code>. If no such mark
     * passing has been reported (yet), <code>null</code> is returned.
     */
    MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint);

    /**
     * Yields the track describing <code>mark</code>'s movement over time; never <code>null</code> because a new track
     * will be created in case no track was present for <code>mark</code> so far.
     */
    GPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark);

    /**
     * Retrieves all marks assigned to the race. They are not necessarily part of the race course.
     */
    Iterable<Mark> getMarks();

    /**
     * If the <code>waypoint</code> only has one {@link #getMarks() mark}, its position at time <code>timePoint</code>
     * is returned. Otherwise, the center of gravity between the mark positions is computed and returned.
     */
    Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint);

    /**
     * Same as {@link #getWind(Position, TimePoint, Iterable) getWind(p, at, Collections.emptyList())}
     */
    Wind getWind(Position p, TimePoint at);

    /**
     * Obtains estimated interpolated wind information for a given position and time point. The information is taken
     * from all wind sources available except for those listed in <code>windSourcesToExclude</code>, using the
     * confidences of the wind values provided by the various sources during averaging.
     */
    Wind getWind(Position p, TimePoint at, Iterable<WindSource> windSourcesToExclude);

    /**
     * Retrieves the wind sources used so far by this race that have the specified <code>type</code> as their
     * {@link WindSource#getType() type}. Always returns a non-<code>null</code> iterable which may be empty in case the
     * race does not use any wind source of the specified type (yet). Additional sources may be returned after
     * 
     */
    Iterable<WindSource> getWindSources(WindSourceType type);

    /**
     * Retrieves all wind sources used by this race. Callers can freely iterate because a copied collection is returned.
     */
    Iterable<WindSource> getWindSources();

    /**
     * Same as {@link #getOrCreateWindTrack(WindSource, long) getOrCreateWindTrack(windSource,
     * getMillisecondsOverWhichToAverageWind())}.
     */
    WindTrack getOrCreateWindTrack(WindSource windSource);

    WindTrack getOrCreateWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation);

    /**
     * Waits until {@link #getUpdateCount()} is after <code>sinceUpdate</code>.
     */
    void waitForNextUpdate(int sinceUpdate) throws InterruptedException;

    /**
     * Time stamp of the start of the actual tracking. The value can be null (e.g. if we have not received any signal
     * from the tracking infrastructure)
     */
    TimePoint getStartOfTracking();

    /**
     * Time stamp of the end of the actual tracking. The value can be null (e.g. if we have not received any signal from
     * the tracking infrastructure)
     */
    TimePoint getEndOfTracking();

    /**
     * Regardless of the order in which events were received, this method returns the latest time point contained by any
     * of the events received and processed.
     */
    TimePoint getTimePointOfNewestEvent();

    /**
     * Regardless of the order in which events were received, this method returns the oldest time point contained by any
     * of the events received and processed.
     */
    TimePoint getTimePointOfOldestEvent();

    /**
     * @return the mark passings for <code>competitor</code> in this race received so far; the mark passing objects are
     *         returned such that their {@link MarkPassing#getWaypoint() waypoints} are ordered in the same way they are
     *         ordered in the race's {@link Course}. Note, that this doesn't necessarily guarantee ascending time
     *         points, particularly if premature mark passings have been detected accidentally as can be the case with
     *         some tracking providers such as TracTrac. If the caller wants to iterate on the resulting collection or
     *         construct a {@link SortedSet#headSet(Object)} or {@link SortedSet#tailSet(Object)} and then iterate over
     *         that, the caller needs to invoke {@link #lockForRead(Iterable)} with the collection returned as parameter
     *         because insertions into the competitor's mark passing collection will obtain the corresponding write
     *         lock.
     */
    NavigableSet<MarkPassing> getMarkPassings(Competitor competitor);

    void lockForRead(Iterable<MarkPassing> markPassings);

    void unlockAfterRead(Iterable<MarkPassing> markPassings);

    /**
     * Time stamp that the event received last from the underlying push service carried on it. Note that these times may
     * not increase monotonically.
     */
    TimePoint getTimePointOfLastEvent();

    long getMillisecondsOverWhichToAverageSpeed();

    long getMillisecondsOverWhichToAverageWind();

    /**
     * Gets the current delay of incoming events to the real time of the events in milliseconds
     */
    long getDelayToLiveInMillis();

    /**
     * Estimates the wind direction based on the observed boat courses at the time given for the position provided. The
     * estimate is based on the assumption that the boats which are on an upwind or a downwind leg sail with very
     * similar angles on the starboard and the port side. There should be clusters of courses which are close to each
     * other (within a threshold of, say, +/- 5 degrees), whereas for the upwind group there should be two clusters with
     * angles about 90 degrees apart; similarly, for the downwind leg there should be two clusters, only that the
     * general jibing angle may vary more, based on the wind speed and the boat class.
     * <p>
     * 
     * Boats {@link GPSFixTrack#hasDirectionChange(TimePoint, double) currently maneuvering} are not considered for this
     * analysis.
     * <p>
     * 
     * This wind direction should not be used directly to compute the leg's wind direction and hence the {@link LegType
     * leg type} because an endless recursion may result: an implementation of this method signature will need to know
     * whether a leg is an upwind or downwind leg for which it has to know where the wind is coming from.
     * 
     * @return <code>null</code> if no sufficient boat track information is available or leg type identification (upwind
     *         vs. downwind) is not possible; a valid {@link Wind} fix otherwise whose bearing is inferred from the boat
     *         courses and whose speed in knots is currently a rough indication of how many boats' courses contributed
     *         to determining the bearing. If in the future we have data about polar diagrams specific to boat classes,
     *         we may be able to also infer the wind speed from the boat tracks.
     */
    Wind getEstimatedWindDirection(Position position, TimePoint timePoint);

    /**
     * Determines whether the <code>competitor</code> is sailing on port or starboard tack at the <code>timePoint</code>
     * requested. Note that this will have to retrieve information about the wind. This, in turn, can lead to the
     * current thread obtaining the monitor of the various wind tracks, and, if the
     * {@link WindSource#TRACK_BASED_ESTIMATION} source is used, also the monitors of the competitors' GPS tracks.
     */
    Tack getTack(Competitor competitor, TimePoint timePoint) throws NoWindException;

    TrackedRegatta getTrackedRegatta();

    /**
     * Computes a default wind direction based on the direction of the first leg at time <code>at</code>, with a default
     * speed of one knot. Note that this wind direction can only be used if {@link #raceIsKnownToStartUpwind()} returns
     * <code>true</code>.
     * 
     * @param at
     *            usually the {@link #getStart() start time} should be used; if no valid start time is provided, the
     *            current time point may serve as a default
     * @return <code>null</code> in case the first leg's direction cannot be determined, e.g., because the necessary
     *         mark positions are not known (yet)
     */
    Wind getDirectionFromStartToNextMark(TimePoint at);

    /**
     * Uses a {@link DouglasPeucker Douglas-Peucker} algorithm to approximate this track's fixes starting at time
     * <code>from</code> until time point <code>to</code> such that the maximum distance between the track's fixes and
     * the approximation is at most <code>maxDistance</code>.
     */
    List<GPSFixMoving> approximate(Competitor competitor, Distance maxDistance, TimePoint from, TimePoint to);

    /**
     * @return a non-<code>null</code> but perhaps empty list of the maneuvers that <code>competitor</code> performed in
     *         this race between <code>from</code> and <code>to</code>. Depending on <code>waitForLatest</code> the
     *         result is taken from the cache straight away (<code>waitForLatest==false</code>) or, if a re-calculation
     *         for the <code>key</code> is still ongoing, the result of that ongoing re-calculation is returned.
     */
    List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to, boolean waitForLatest)
            throws NoWindException;

    /**
     * @return <code>true</code> if this race is known to start with an {@link LegType#UPWIND upwind} leg. If this is
     *         the case, the wind estimation may default to using the first leg's direction at race start time as the
     *         direction the wind comes from.
     */
    boolean raceIsKnownToStartUpwind();

    /**
     * Many calculations require valid wind data. In order to prevent NoWindException's to be handled by those
     * calculation this method can be used to check whether the tracked race has sufficient wind information available.
     * 
     * @return <code>true</code> if {@link #getWind(Position, TimePoint)} delivers a (not null) wind fix.
     */
    boolean hasWindData();

    /**
     * 
     * @return <code>true</code> if at least one GPS fix for one of the competitors is available for this race.
     */
    boolean hasGPSData();

    /**
     * Adds a race change listener to the set of listeners that will be notified about changes to this race. The
     * listener won't be serialized together with this object.
     */
    void addListener(RaceChangeListener listener);

    void removeListener(RaceChangeListener listener);

    Distance getDistanceTraveled(Competitor competitor, TimePoint timePoint);

    Distance getWindwardDistanceToOverallLeader(Competitor competitor, TimePoint timePoint) throws NoWindException;

    /**
     * Calls {@link #getWindWithConfidence(Position, TimePoint, Iterable)} and excludes those wind sources listed in
     * {@link #getWindSourcesToExclude}.
     */
    WindWithConfidence<Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at);

    /**
     * Lists those wind sources which by default are not considered in {@link #getWind(Position, TimePoint)} and
     * {@link #getWindWithConfidence(Position, TimePoint)}.
     */
    Iterable<WindSource> getWindSourcesToExclude();

    /**
     * Loops over this tracked race's wind sources and from each asks its averaged wind for the position <code>p</code>
     * and time point <code>at</code>, using the particular wind source's averaging interval. The confidences delivered
     * by each wind source are used during computing the averaged result across the wind sources. The result has the
     * averaged confidence attached.
     */
    WindWithConfidence<Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at,
            Iterable<WindSource> windSourcesToExclude);

    /**
     * Same as {@link #getEstimatedWindDirection(Position, TimePoint)}, but propagates the confidence of the wind
     * estimation, relative to the <code>timePoint</code> for which the request is made, in the result.
     */
    WindWithConfidence<TimePoint> getEstimatedWindDirectionWithConfidence(Position position, TimePoint timePoint);

    /**
     * After the call returns, {@link #getWindSourcesToExclude()} returns an iterable that equals
     * <code>windSourcesToExclude</code>
     */
    void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude);

    /**
     * Computes the average cross-track error for the legs with type {@link LegType#UPWIND}.
     * 
     * @param waitForLatestAnalysis
     *            if <code>true</code> and any cache update is currently going on, wait for the update to complete and
     *            then fetch the updated value; otherwise, serve this requests from whatever is currently in the cache
     */
    Distance getAverageCrossTrackError(Competitor competitor, TimePoint timePoint, boolean waitForLatestAnalysis)
            throws NoWindException;

    WindStore getWindStore();

    Competitor getOverallLeader(TimePoint timePoint) throws NoWindException;

    /**
     * Returns the competitors of this tracked race, according to their ranking. Competitors whose
     * {@link #getRank(Competitor)} is 0 will be sorted "worst".
     */
    List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) throws NoWindException;

    Distance getAverageCrossTrackError(Competitor competitor, TimePoint from, TimePoint to, boolean upwindOnly,
            boolean waitForLatestAnalyses) throws NoWindException;

    /**
     * When provided with a {@link WindStore} during construction, the tracked race will asynchronously load the wind
     * data for this tracked race from the wind store in a background thread and update this tracked race with the
     * results. Clients that want to wait for the wind loading process to complete can do so by calling this method
     * which will block until the wind loading has completed.
     */
    void waitUntilWindLoadingComplete() throws InterruptedException;
    
    TrackedRaceStatus getStatus();

    /**
     * If the {@link #getStatus() status} is currently {@link TrackedRaceStatusEnum#LOADING}, blocks until the status changes to any
     * other status.
     */
    void waitUntilNotLoading();
}
