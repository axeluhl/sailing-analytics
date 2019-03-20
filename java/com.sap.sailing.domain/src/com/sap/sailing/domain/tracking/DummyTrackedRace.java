package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.ranking.RankingMetric;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class DummyTrackedRace extends TrackedRaceWithWindEssentials {
    private static final long serialVersionUID = -11522605089325440L;
    private Regatta regatta;

    public DummyTrackedRace(final Serializable raceId, final Map<Competitor, Boat> competitorsAndBoats, final Regatta regatta, final TrackedRegatta trackedRegatta, final WindStore windStore) {
        this(competitorsAndBoats, regatta, trackedRegatta, new RaceDefinitionImpl("DummyRace", new CourseImpl("Dummy Course", Collections.<Waypoint>emptyList()),
                regatta.getBoatClass(), competitorsAndBoats, raceId), windStore);
    }
    
    public DummyTrackedRace(final Map<Competitor, Boat> competitors, final Regatta regatta, final TrackedRegatta trackedRegatta,
            RaceDefinition race, WindStore windStore) {
        super(race, trackedRegatta, windStore, -1);
        this.regatta = regatta;
    }

    public DummyTrackedRace(final Map<Competitor, Boat> competitors, final Regatta regatta, final TrackedRegatta trackedRegatta,
            RaceDefinition race) {
       this(competitors, regatta, trackedRegatta, race, EmptyWindStore.INSTANCE);
        
    }

    public DummyTrackedRace(final String raceName, final Serializable raceId) {
        super(new RaceDefinitionImpl(raceName, new CourseImpl("Dummy Course", Collections.<Waypoint> emptyList()),
                /* boatClass */ null, Collections.<Competitor, Boat>emptyMap(), raceId), null, EmptyWindStore.INSTANCE, -1);
    }

    @Override
    public RaceDefinition getRace() {
        return race;
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return null;
    }

    @Override
    public TimePoint getStartOfRace() {
        return null;
    }

    @Override
    public TimePoint getStartOfRace(boolean inferred) {
        return null;
    }

    @Override
    public TimePoint getFinishedTime() {
        return null;
    }

    @Override
    public TimePoint getEndOfRace() {
        return null;
    }

    @Override
    public Iterable<Util.Pair<Waypoint, Util.Pair<TimePoint, TimePoint>>> getMarkPassingsTimes() {
        return null;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        return false;
    }

    @Override
    public Iterable<TrackedLeg> getTrackedLegs() {
        return null;
    }

    @Override
    public TrackedLeg getTrackedLeg(Leg leg) {
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public TrackedLeg getCurrentLeg(TimePoint timePoint) {
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        return null;
    }

    @Override
    public GPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at) {
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg) {
        return null;
    }

    @Override
    public long getUpdateCount() {
        return 0;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        return 0;
    }

    @Override
    public int getRank(Competitor competitor) throws NoWindException {
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        return 0;
    }

    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return null;
    }

    @Override
    public MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint) {
        return null;
    }

    @Override
    public GPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark) {
        return null;
    }

    @Override
    public Iterable<Mark> getMarks() {
        return null;
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint,
            MarkPositionAtTimePointCache markPositionCache) {
        return null;
    }

    @Override
    public Wind getWind(Position p, TimePoint at) {
        return null;
    }

    @Override
    public Wind getWind(Position p, TimePoint at, Set<WindSource> windSourcesToExclude) {
        return null;
    }

    @Override
    public void waitForNextUpdate(int sinceUpdate) throws InterruptedException {
    }

    @Override
    public TimePoint getStartOfTracking() {
        return null;
    }

    @Override
    public TimePoint getEndOfTracking() {
        return null;
    }

    @Override
    public TimePoint getTimePointOfNewestEvent() {
        return null;
    }

    @Override
    public TimePoint getTimePointOfOldestEvent() {
        return null;
    }

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        return null;
    }

    @Override
    public void lockForRead(Iterable<MarkPassing> markPassings) {
    }

    @Override
    public void unlockAfterRead(Iterable<MarkPassing> markPassings) {
    }

    @Override
    public TimePoint getTimePointOfLastEvent() {
        return null;
    }

    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        return 0;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        return 0;
    }

    @Override
    public long getDelayToLiveInMillis() {
        return 0;
    }

    @Override
    public Wind getEstimatedWindDirection(TimePoint timePoint) {
        return null;
    }

    @Override
    public Tack getTack(Competitor competitor, TimePoint timePoint) throws NoWindException {
        return null;
    }

    @Override
    public Tack getTack(SpeedWithBearing speedWithBearing, Wind wind, TimePoint timePoint) {
        return null;
    }

    @Override
    public TrackedRegatta getTrackedRegatta() {
        return new DynamicTrackedRegattaImpl(regatta);
    }

    @Override
    public Wind getDirectionFromStartToNextMark(TimePoint at) {
        return null;
    }

    @Override
    public List<GPSFixMoving> approximate(Competitor competitor, Distance maxDistance, TimePoint from, TimePoint to) {
        return null;
    }

    @Override
    public Iterable<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to, boolean waitForLatest) {
        return null;
    }
    
    @Override
    public Iterable<Maneuver> getManeuvers(Competitor competitor, boolean waitForLatest) {
        return null;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        return false;
    }

    @Override
    public boolean hasWindData() {
        return false;
    }

    @Override
    public boolean hasGPSData() {
        return false;
    }

    @Override
    public void addListener(RaceChangeListener listener) {
    }

    @Override
    public void addListener(RaceChangeListener listener, boolean notifyAboutWindFixesAlreadyLoaded,
            boolean notifyAboutGPSFixesAlreadyLoaded) {
    }

    @Override
    public void removeListener(RaceChangeListener listener) {

    }

    @Override
    public Distance getDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getDistanceFoiled(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Duration getDurationFoiled(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getWindwardDistanceToCompetitorFarthestAhead(Competitor competitor, TimePoint timePoint,
            WindPositionMode windPositionMode) {
        return null;
    }

    @Override
    public WindWithConfidence<Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at) {
        return null;
    }

    @Override
    public Set<WindSource> getWindSourcesToExclude() {
        return null;
    }

    @Override
    public WindWithConfidence<Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at,
            Set<WindSource> windSourcesToExclude) {
        return null;
    }

    @Override
    public WindWithConfidence<TimePoint> getEstimatedWindDirectionWithConfidence(TimePoint timePoint) {
        return null;
    }

    @Override
    public void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude) {

    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalysis) throws NoWindException {
        return null;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalysis) throws NoWindException {
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint from, TimePoint to,
            boolean upwindOnly, boolean waitForLatestAnalysis) throws NoWindException {
        return null;
    }

    @Override
    public WindStore getWindStore() {
        return null;
    }

    @Override
    public Competitor getOverallLeader(TimePoint timePoint) {
        return null;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint from, TimePoint to,
            boolean upwindOnly, boolean waitForLatestAnalyses) throws NoWindException {
        return null;
    }

    @Override
    public void waitUntilLoadingFromWindStoreComplete() throws InterruptedException {
    }

    @Override
    public TrackedRaceStatus getStatus() {
        return null;
    }

    @Override
    public void waitUntilNotLoading() {
    }

    @Override
    public RaceLog detachRaceLog(Serializable identifier) {
        return null;
    }

    @Override
    public void attachRaceLog(RaceLog raceLog) {
    }

    @Override
    public RaceLog getRaceLog(Serializable identifier) {
        return null;
    }

    @Override
    public void addCourseDesignChangedListener(CourseDesignChangedListener listener) {

    }

    @Override
    public Distance getDistanceToStartLine(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getDistanceFromStarboardSideOfStartLineWhenPassingStart(Competitor competitor) {
        return null;
    }

    @Override
    public boolean isLive(TimePoint at) {
        return false;
    }

    @Override
    public Iterable<Sideline> getCourseSidelines() {
        return null;
    }

    @Override
    public Distance getDistanceToStartLine(Competitor competitor, long millisecondsBeforeRaceStart) {
        return null;
    }

    @Override
    public Speed getSpeed(Competitor competitor, long millisecondsBeforeRaceStart) {
        return null;
    }

    @Override
    public void addStartTimeChangedListener(StartTimeChangedListener listener) {
    }

    @Override
    public void removeStartTimeChangedListener(StartTimeChangedListener listener) {
    }

    @Override
    public TimePoint getStartTimeReceived() {
        return null;
    }

    @Override
    public LineDetails getStartLine(TimePoint at) {
        return null;
    }

    @Override
    public LineDetails getFinishLine(TimePoint at) {
        return null;
    }

    @Override
    public SpeedWithConfidence<TimePoint> getAverageWindSpeedWithConfidence(long resolutionInMillis) {
        return null;
    }

    @Override
    public Distance getCourseLength() {
        return null;
    }

    @Override
    public Speed getSpeedWhenCrossingStartLine(Competitor competitor) {
        return null;
    }

    @Override
    public Distance getDistanceFromStarboardSideOfStartLine(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public void addRaceAbortedListener(RaceAbortedListener listener) {
    }

    @Override
    public Position getCenterOfCourse(TimePoint at) {
        return null;
    }

    @Override
    public void attachRegattaLog(RegattaLog regattaLog) {
    }

    @Override
    public void waitForLoadingToFinish() throws InterruptedException {
    }

    @Override
    public Boolean isGateStart() {
        return null;
    }

    @Override
    public Distance getDistanceTraveledIncludingGateStart(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getAdditionalGateStartDistance(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public GPSFixTrack<Mark, GPSFix> getTrack(Mark mark) {
        return null;
    }

    @Override
    public long getGateStartGolfDownTime() {
        return 0;
    }

    @Override
    public boolean isUsingMarkPassingCalculator() {
        return false;
    }

    @Override
    public int getLastLegStarted(TimePoint timePoint) {
        return 0;
    }

    @Override
    public void setPolarDataService(PolarDataService polarDataService) {
    }

    @Override
    public TargetTimeInfo getEstimatedTimeToComplete(TimePoint timepoint)
            throws NotEnoughDataHasBeenAddedException, NoWindException {
        return null;
    }

    @Override
    public Distance getWindwardDistanceToCompetitorFarthestAhead(Competitor competitor, TimePoint timePoint,
            WindPositionMode windPositionMode, RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public Competitor getOverallLeader(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor) {
        return null;
    }
    
    @Override
    public Competitor getCompetitorOfBoat(Boat boat) {
        return null;
    }

    @Override
    public RankingMetric getRankingMetric() {
        return null;
    }

    @Override
    public void attachRaceExecutionProvider(RaceExecutionOrderProvider raceExecutionOrderProvider) {
    }

    @Override
    public void detachRaceExecutionOrderProvider(RaceExecutionOrderProvider raceExecutionOrderProvider) {
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        return this;
    }

    @Override
    public void updateStartAndEndOfTracking(boolean waitForGPSFixesToLoad) {
    }

    @Override
    public <FixT extends SensorFix, TrackT extends SensorFixTrack<Competitor, FixT>> TrackT getSensorTrack(
            Competitor competitor, String trackName) {
        return null;
    }

    @Override
    public Iterable<RegattaLog> getAttachedRegattaLogs() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<RaceLog> getAttachedRaceLogs() {
        return Collections.emptySet();
    }

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor, boolean waitForLatestUpdates) {
        return null;
    }

    @Override
    public Distance getAverageRideHeight(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getEstimatedDistanceToComplete(TimePoint now) {
        return null;
    }

    @Override
    public <FixT extends SensorFix, TrackT extends SensorFixTrack<Competitor, FixT>> Iterable<TrackT> getSensorTracks(
            String trackName) {
        return Collections.emptySet();
    }

    @Override
    public Speed getAverageSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Tack getTack(Position where, TimePoint timePoint, Bearing boatBearing) throws NoWindException {
        return null;
    }

    @Override
    public SpeedWithBearing getVelocityMadeGood(Competitor competitor, TimePoint timePoint,
            WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        return null;
    }

    @Override
    public WindSummary getWindSummary() {
        return null;
    }
}
