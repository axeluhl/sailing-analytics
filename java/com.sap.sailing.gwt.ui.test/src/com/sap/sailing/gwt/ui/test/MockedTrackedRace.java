package com.sap.sailing.gwt.ui.test;

import java.io.Serializable;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
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
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
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
import com.sap.sailing.domain.tracking.Wind;
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
    private final WindTrack windTrack = new WindTrackImpl(/* millisecondsOverWhichToAverage */ 30000, /* useSpeed */ true, "TestWindTrack");

    public WindTrack getWindTrack() {
        return windTrack;
    }

    @Override
    public RaceDefinition getRace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getStartOfRace() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0;
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
    public DynamicGPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark) {
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
    public TimePoint getTimePointOfNewestEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

    @Override
    public void addListener(RaceChangeListener listener, boolean notifyAboutWindFixesAlreadyLoaded,
            boolean notifyAboutGPSFixesAlreadyLoaded) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStartTimeReceived(TimePoint start) {
        // TODO Auto-generated method stub

    }

    @Override
    public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeWind(Wind wind, WindSource windSource) {
        // TODO Auto-generated method stub

    }

    @Override
    public TimePoint getTimePointOfLastEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
        // TODO Auto-generated method stub

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
    public Wind getEstimatedWindDirection(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        // TODO Auto-generated method stub
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
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Iterable<RaceDefinition> getAllRaces() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public BoatClass getBoatClass() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Iterable<Competitor> getCompetitors() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void addRace(RaceDefinition race) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void removeRace(RaceDefinition raceDefinition) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public RaceDefinition getRaceByName(String raceName) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void addRegattaListener(RegattaListener listener) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void removeRegattaListener(RegattaListener listener) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public RegattaIdentifier getRegattaIdentifier() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Iterable<? extends Series> getSeries() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Series getSeriesByName(String seriesName) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean isPersistent() {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void addRaceColumnListener(RaceColumnListener listener) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void removeRaceColumnListener(RaceColumnListener listener) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public ScoringScheme getScoringScheme() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public CourseArea getDefaultCourseArea() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void setDefaultCourseArea(CourseArea newCourseArea) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public boolean definesSeriesDiscardThresholds() {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public RegattaAndRaceIdentifier getRaceIdentifier(RaceDefinition race) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RegattaConfiguration getRegattaConfiguration() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void setRegattaConfiguration(RegattaConfiguration configuration) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void addSeries(Series series) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public boolean useStartTimeInference() {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void removeSeries(Series series) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void setUseStartTimeInference(boolean useStartTimeInference) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public TimePoint getStartDate() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void setStartDate(TimePoint startDate) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public TimePoint getEndDate() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void setEndDate(TimePoint startDate) {
                        // TODO Auto-generated method stub
                        
                    }
                };
            }

            @Override
            public Iterable<DynamicTrackedRace> getTrackedRaces() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void addTrackedRace(TrackedRace trackedRace) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeTrackedRace(TrackedRace trackedRace) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addRaceListener(RaceListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void removeTrackedRace(RaceDefinition raceDefinition) {
                // TODO Auto-generated method stub

            }

            @Override
            public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines, WindStore windStore,
                    GPSFixStore gpsFixStore, long delayToLiveInMillis, long millisecondsOverWhichToAverageWind,
                    long millisecondsOverWhichToAverageSpeed, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
                    boolean useMarkPassingCalculator) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint) {
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
    public void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind) {
        // TODO Auto-generated method stub
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getEndOfRace() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<WindSource> getWindSources() {
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
    public TimePoint getEndOfTracking() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfOldestEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStartOfTrackingReceived(TimePoint startOfTrackingReceived) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEndOfTrackingReceived(TimePoint endOfTrackingReceived) {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterable<Util.Pair<Waypoint, Util.Pair<TimePoint, TimePoint>>> getMarkPassingsTimes() {
        // TODO Auto-generated method stub
        return null;
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
    public WindTrack getOrCreateWindTrack(WindSource windSource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recordFix(Mark mark, GPSFix fix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeListener(RaceChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public WindStore getWindStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setWindSourcesToExclude(Iterable<? extends WindSource> windSourcesToExclude) {
        // TODO Auto-generated method stub
    }

    @Override
    public Competitor getOverallLeader(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDelayToLiveInMillis() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDelayToLiveInMillis(long delayToLiveInMillis) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAndFixDelayToLiveInMillis(long delayToLiveInMillis) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(Competitor competitor, TimePoint from, TimePoint to, boolean upwindOnly, boolean waitForLatestAnalyses)
            throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitUntilLoadingFromWindStoreComplete() throws InterruptedException {
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
    public void setStatus(TrackedRaceStatus newStatus) {
        // TODO Auto-generated method stub
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
    public void onCourseDesignChangedByRaceCommittee(CourseBase courseDesign) {
        // TODO Auto-generated method stub
        
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
    public void invalidateStartTime() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void invalidateEndTime() {
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
    
    public void onStartTimeChangedByRaceCommittee(TimePoint newStartTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
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
    public GPSFixStore getGPSFixStore() {
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
    public void waitForLoadingFromGPSFixStoreToFinishRunning(RaceLog rorRaceLog) throws InterruptedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addRaceAbortedListener(RaceAbortedListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAbortedByRaceCommittee(Flags flag) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Position getCenterOfCourse(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUsingMarkPassingCalculator() {
        // TODO Auto-generated method stub
        return false;
    }

}
