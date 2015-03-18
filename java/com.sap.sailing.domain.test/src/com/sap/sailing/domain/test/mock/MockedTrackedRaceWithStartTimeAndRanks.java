package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceAbortedListener;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Simple mock for {@link TrackedRace} for leaderboard testing; the leaderboard only requests {@link #hasStarted(TimePoint)} and
 * {@link #getRank(Competitor)} and {@link #getRank(Competitor, TimePoint)}. Additionally, a mocked {@link RaceDefinition} is produced
 * from the competitor list.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class MockedTrackedRaceWithStartTimeAndRanks implements TrackedRace {
    private static final long serialVersionUID = 2708044935347796930L;
    private final TimePoint startTime;
    private final List<Competitor> competitorsFromBestToWorst;
    private final Regatta regatta;
    private RaceDefinition race;

    /**
     * @param competitorsFromBestToWorst
     *            copied, so not live; the list passed may change afterwards without effects on the rankings in this
     *            mocked tracked race
     */
    public MockedTrackedRaceWithStartTimeAndRanks(TimePoint startTime, List<Competitor> competitorsFromBestToWorst) {
        this(startTime, competitorsFromBestToWorst, null);
    }
    
    public MockedTrackedRaceWithStartTimeAndRanks(TimePoint startTime, List<Competitor> competitorsFromBestToWorst, Regatta regatta) {
        this.regatta = regatta;
        this.startTime = startTime;
        // copies the list to make sure that later modifications to the list passed to this constructor don't affect the ranking produced by this race
        this.competitorsFromBestToWorst = new ArrayList<Competitor>(competitorsFromBestToWorst);
        final List<Waypoint> waypoints = Collections.emptyList();
        this.race = new RaceDefinitionImpl("Mocked Race", new CourseImpl("Mock Course", waypoints), /* boat class */ null,
                competitorsFromBestToWorst);
    }

    @Override
    public RaceDefinition getRace() {
        return race;
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getStartOfRace() {
        return startTime;
    }

    @Override
    public TimePoint getEndOfRace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Util.Pair<Waypoint, Util.Pair<TimePoint, TimePoint>>> getMarkPassingsTimes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        return at.compareTo(startTime) >= 0;
    }

    @Override
    public Iterable<TrackedLeg> getTrackedLegs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLeg(Leg leg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getCurrentLeg(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getUpdateCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor) throws NoWindException {
        return competitorsFromBestToWorst.indexOf(competitor) + 1;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        return competitorsFromBestToWorst.indexOf(competitor) + 1;
    }

    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Wind getWind(Position p, TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Wind getWind(Position p, TimePoint at, Set<WindSource> windSourcesToExclude) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<WindSource> getWindSources(WindSourceType type) {
        return Collections.emptySet();
    }

    @Override
    public Set<WindSource> getWindSources() {
        return Collections.emptySet();
    }

    @Override
    public WindTrack getOrCreateWindTrack(WindSource windSource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindTrack getOrCreateWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitForNextUpdate(int sinceUpdate) throws InterruptedException {
        // TODO Auto-generated method stub

    }

    @Override
    public TimePoint getStartOfTracking() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getEndOfTracking() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfNewestEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfOldestEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfLastEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getDelayToLiveInMillis() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Wind getEstimatedWindDirection(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tack getTack(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Tack getTack(SpeedWithBearing speedWithBearing, Wind wind, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedRegatta getTrackedRegatta() {
        return new DynamicTrackedRegattaImpl(regatta);
    }

    @Override
    public Wind getDirectionFromStartToNextMark(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GPSFixMoving> approximate(Competitor competitor, Distance maxDistance, TimePoint from, TimePoint to) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to, boolean waitForLatest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addListener(RaceChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addListener(RaceChangeListener listener, boolean notifyAboutWindFixesAlreadyLoaded,
            boolean notifyAboutGPSFixesAlreadyLoaded) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeListener(RaceChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public Distance getDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getWindwardDistanceToOverallLeader(Competitor competitor, TimePoint timePoint, WindPositionMode windPositionMode)
            throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindWithConfidence<Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<WindSource> getWindSourcesToExclude() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindWithConfidence<Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at,
            Set<WindSource> windSourcesToExclude) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindWithConfidence<TimePoint> getEstimatedWindDirectionWithConfidence(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude) {
        // TODO Auto-generated method stub

    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint timePoint, boolean waitForLatestAnalysis) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Competitor getOverallLeader(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        return competitorsFromBestToWorst;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint from, TimePoint to, boolean upwindOnly, boolean waitForLatestAnalyses)
            throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitUntilLoadingFromWindStoreComplete() {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterable<Mark> getMarks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasWindData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasGPSData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void lockForRead(Iterable<MarkPassing> markPassings) {
        // TODO Auto-generated method stub
    }

    @Override
    public void unlockAfterRead(Iterable<MarkPassing> markPassings) {
        // TODO Auto-generated method stub
    }

    @Override
    public TrackedRaceStatus getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitUntilNotLoading() {
        // TODO Auto-generated method stub
    }

    @Override
    public void detachRaceLog(Serializable identifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public void attachRaceLog(RaceLog raceLog) {
        // TODO Auto-generated method stub

    }

    @Override
    public RaceLog getRaceLog(Serializable identifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addCourseDesignChangedListener(CourseDesignChangedListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Distance getDistanceToStartLine(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getDistanceFromStarboardSideOfStartLineWhenPassingStart(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void detachAllRaceLogs() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isLive(TimePoint at) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<Sideline> getCourseSidelines() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getDistanceToStartLine(Competitor competitor, long millisecondsBeforeRaceStart) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getSpeed(Competitor competitor, long millisecondsBeforeRaceStart) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void addStartTimeChangedListener(StartTimeChangedListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TimePoint getStartTimeReceived() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LineDetails getStartLine(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LineDetails getFinishLine(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithConfidence<TimePoint> getAverageWindSpeedWithConfidence(long resolutionInMillis) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getCourseLength() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getSpeedWhenCrossingStartLine(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getDistanceFromStarboardSideOfStartLine(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalysis) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(Competitor competitor, TimePoint from, TimePoint to,
            boolean upwindOnly, boolean waitForLatestAnalysis) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GPSFixStore getGPSFixStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitForLoadingFromGPSFixStoreToFinishRunning(RaceLog forRaceLog) throws InterruptedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addRaceAbortedListener(RaceAbortedListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Position getCenterOfCourse(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attachRegattaLog(RegattaLog regattaLog) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void waitForLoadingFromGPSFixStoreToFinishRunning(RegattaLog fromRegattaLog) throws InterruptedException {
        // TODO Auto-generated method stub
        
    }

}
