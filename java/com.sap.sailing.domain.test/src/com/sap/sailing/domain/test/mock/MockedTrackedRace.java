package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventFetcher;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeListener;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceAbortedListener;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class MockedTrackedRace implements DynamicTrackedRace {
    private static final long serialVersionUID = 5827912985564121181L;
    private final WindTrack windTrack = new WindTrackImpl(/* millisecondsOverWhichToAverage */30000, /* useSpeed */
    true, "TestWindTrack");

    public WindTrack getWindTrack() {
        return windTrack;
    }

    @Override
    public RaceDefinition getRace() {
        return null;
    }

    @Override
    public TimePoint getStartOfRace() {
        return null;
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
    public DynamicGPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark) {
        return null;
    }

    @Override
    public WindTrack getOrCreateWindTrack(WindSource windSource, long delayForWindEstimationCacheInvalidation) {
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
    public TimePoint getTimePointOfNewestEvent() {
        return null;
    }

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        return new TreeSet<MarkPassing>();
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
    }

    @Override
    public boolean recordWind(Wind wind, WindSource windSource) {
        if (windSource.getType() == WindSourceType.EXPEDITION) {
            windTrack.add(wind);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addListener(RaceChangeListener listener) {
    }

    @Override
    public void addListener(RaceChangeListener listener, boolean notifyAboutWindFixesAlreadyLoaded,
            boolean notifyAboutGPSFixesAlreadyLoaded) {
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
    }

    @Override
    public void setStartTimeReceived(TimePoint start) {
    }

    @Override
    public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return null;
    }

    @Override
    public void removeWind(Wind wind, WindSource windSource) {
    }

    @Override
    public TimePoint getTimePointOfLastEvent() {
        return null;
    }

    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
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
    public Wind getEstimatedWindDirection(TimePoint timePoint) {
        return null;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        return false;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return new DynamicTrackedRegatta() {
            private static final long serialVersionUID = 2651590861333064588L;

            @Override
            public Regatta getRegatta() {
                return new Regatta() {
                    private static final long serialVersionUID = -4908774269425170811L;

                    @Override
                    public String getName() {
                        return "A Mocked Test Regatta";
                    }

                    @Override
                    public Serializable getId() {
                        return null;
                    }

                    @Override
                    public Iterable<RaceDefinition> getAllRaces() {
                        return null;
                    }

                    @Override
                    public BoatClass getBoatClass() {
                        return null;
                    }

                    @Override
                    public Iterable<Competitor> getAllCompetitors() {
                        return null;
                    }

                    @Override
                    public void addRace(RaceDefinition race) {
                    }

                    @Override
                    public void removeRace(RaceDefinition raceDefinition) {
                    }

                    @Override
                    public RaceDefinition getRaceByName(String raceName) {
                        return null;
                    }

                    @Override
                    public void addRegattaListener(RegattaListener listener) {
                    }

                    @Override
                    public void removeRegattaListener(RegattaListener listener) {
                    }

                    @Override
                    public RegattaIdentifier getRegattaIdentifier() {
                        return null;
                    }

                    @Override
                    public Iterable<? extends Series> getSeries() {
                        return null;
                    }

                    @Override
                    public Series getSeriesByName(String seriesName) {
                        return null;
                    }

                    @Override
                    public boolean isPersistent() {
                        return false;
                    }

                    @Override
                    public void addRaceColumnListener(RaceColumnListener listener) {
                    }

                    @Override
                    public void removeRaceColumnListener(RaceColumnListener listener) {
                    }

                    @Override
                    public ScoringScheme getScoringScheme() {
                        return null;
                    }

                    @Override
                    public CourseArea getDefaultCourseArea() {
                        return null;
                    }

                    @Override
                    public void setDefaultCourseArea(CourseArea newCourseArea) {
                    }

                    @Override
                    public boolean definesSeriesDiscardThresholds() {
                        return false;
                    }

                    @Override
                    public RegattaAndRaceIdentifier getRaceIdentifier(RaceDefinition race) {
                        return null;
                    }

                    @Override
                    public RegattaConfiguration getRegattaConfiguration() {
                        return null;
                    }

                    @Override
                    public void setRegattaConfiguration(RegattaConfiguration configuration) {
                    }

                    @Override
                    public void addSeries(Series series) {
                    }

                    @Override
                    public boolean useStartTimeInference() {
                        return false;
                    }

                    @Override
                    public void removeSeries(Series series) {
                    }

                    @Override
                    public void setUseStartTimeInference(boolean useStartTimeInference) {
                    }

                    @Override
                    public RegattaLog getRegattaLog() {
                        return null;
                    }

                    @Override
                    public TimePoint getStartDate() {
                        return null;
                    }

                    @Override
                    public void setStartDate(TimePoint startDate) {
                        
                    }

                    @Override
                    public TimePoint getEndDate() {
                        return null;
                    }

                    @Override
                    public void setEndDate(TimePoint startDate) {
                        
                    }

                    @Override
                    public RegattaLikeIdentifier getRegattaLikeIdentifier() {
                        return null;
                    }

                    @Override
                    public void addListener(RegattaLikeListener listener) {                        
                    }

                    @Override
                    public void removeListener(RegattaLikeListener listener) {                        
                    }

                    @Override
                    public void adjustEventToRegattaAssociation(EventFetcher eventFetcher) {
                    }
                };
            }

            @Override
            public Iterable<DynamicTrackedRace> getTrackedRaces() {
                return null;
            }

            @Override
            public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
                return null;
            }

            @Override
            public void addTrackedRace(TrackedRace trackedRace) {
            }

            @Override
            public void removeTrackedRace(TrackedRace trackedRace) {
            }

            @Override
            public void addRaceListener(RaceListener listener) {
            }

            @Override
            public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
                return 0;
            }

            @Override
            public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
                return null;
            }

            @Override
            public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
                return null;
            }

            @Override
            public void removeTrackedRace(RaceDefinition raceDefinition) {
            }

            @Override
            public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines,
                    WindStore windStore, GPSFixStore gpsFixStore, long delayToLiveInMillis,
                    long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
                    DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useMarkPassingcalculator) {
                return null;
            }
        };
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint) {
        return null;
    }

    @Override
    public Tack getTack(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Tack getTack(SpeedWithBearing speedWithBearing, Wind wind, TimePoint timePoint) {
        return null;
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
    public List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to, boolean waitForLatest) {
        return null;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        return false;
    }

    @Override
    public void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind) {
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return null;
    }

    @Override
    public TimePoint getEndOfRace() {
        return null;
    }

    @Override
    public Distance getDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        return null;
    }

    @Override
    public Distance getWindwardDistanceToOverallLeader(Competitor competitor, TimePoint timePoint,
            WindPositionMode windPositionMode) throws NoWindException {
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
    public Set<WindSource> getWindSources(WindSourceType type) {
        return Collections.emptySet();
    }

    @Override
    public Set<WindSource> getWindSources() {
        return Collections.emptySet();
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
    public WindWithConfidence<Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at) {
        return null;
    }

    @Override
    public Set<WindSource> getWindSourcesToExclude() {
        return null;
    }

    @Override
    public TimePoint getEndOfTracking() {
        return null;
    }

    @Override
    public TimePoint getTimePointOfOldestEvent() {
        return null;
    }

    @Override
    public void setStartOfTrackingReceived(TimePoint startOfTrackingReceived) {
    }

    @Override
    public void setEndOfTrackingReceived(TimePoint endOfTrackingReceived) {
    }

    @Override
    public Iterable<Util.Pair<Waypoint, Util.Pair<TimePoint, TimePoint>>> getMarkPassingsTimes() {
        return null;
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
    public WindTrack getOrCreateWindTrack(WindSource windSource) {
        return null;
    }

    @Override
    public void recordFix(Mark mark, GPSFix fix) {
    }

    @Override
    public void removeListener(RaceChangeListener listener) {
    }

    @Override
    public WindStore getWindStore() {
        return null;
    }

    @Override
    public void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude) {
    }

    @Override
    public Competitor getOverallLeader(TimePoint timePoint) throws NoWindException {
        return null;
    }

    @Override
    public long getDelayToLiveInMillis() {
        return 0;
    }

    @Override
    public void setDelayToLiveInMillis(long delayToLiveInMillis) {
    }

    @Override
    public void setAndFixDelayToLiveInMillis(long delayToLiveInMillis) {
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
    public Iterable<Mark> getMarks() {
        return null;
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
    public void lockForRead(Iterable<MarkPassing> markPassings) {
    }

    @Override
    public void unlockAfterRead(Iterable<MarkPassing> markPassings) {
    }

    @Override
    public TrackedRaceStatus getStatus() {
        return null;
    }

    @Override
    public void setStatus(TrackedRaceStatus newStatus) {
    }

    @Override
    public void waitUntilNotLoading() {
    }

    @Override
    public void detachRaceLog(Serializable identifier) {
    }

    @Override
    public void attachRaceLog(RaceLog raceLog) {
    }

    @Override
    public RaceLog getRaceLog(Serializable identifier) {
        return null;
    }

    @Override
    public void onCourseDesignChangedByRaceCommittee(CourseBase courseDesign) {
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
    public void detachAllRaceLogs() {
    }

    @Override
    public void invalidateStartTime() {
    }

    @Override
    public void invalidateEndTime() {
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

    public void onStartTimeChangedByRaceCommittee(TimePoint newStartTime) {
    }

    @Override
    public void addStartTimeChangedListener(StartTimeChangedListener listener) {
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

    public GPSFixStore getGPSFixStore() {
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
    public void waitForLoadingFromGPSFixStoreToFinishRunning(RaceLog forRaceLog) throws InterruptedException {
    }

    @Override
    public void addRaceAbortedListener(RaceAbortedListener listener) {
    }

    @Override
    public void onAbortedByRaceCommittee(Flags flag) {
    }

    @Override
    public Position getCenterOfCourse(TimePoint at) {
        return null;
    }

    @Override
    public boolean isUsingMarkPassingCalculator() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void attachRegattaLog(RegattaLog regattaLog) {
    }

    @Override
    public void waitForLoadingFromGPSFixStoreToFinishRunning(RegattaLog fromRegattaLog) throws InterruptedException {
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
    public int getLastLegStarted(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }
}
