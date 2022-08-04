package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRaceStatsStatement.TrackedRaceWithCompetitorAndStartWaypoint;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class InsertRaceStatsStatement extends AbstractPreparedInsertStatement<TrackedRaceWithCompetitorAndStartWaypoint> {
    private static final Duration DURATION_AFTER_START_TO_DECIDE_START_WINNER = Duration.ONE_SECOND.times(90);
    
    static class TrackedRaceWithCompetitorAndStartWaypoint {
        private final TimePoint now;
        private final Waypoint startWaypoint;
        private final Competitor competitor;
        private final TrackedRace trackedRace;
        public TrackedRaceWithCompetitorAndStartWaypoint(TimePoint now, Waypoint startWaypoint, Competitor competitor,
                TrackedRace trackedRace) {
            super();
            this.now = now;
            this.startWaypoint = startWaypoint;
            this.competitor = competitor;
            this.trackedRace = trackedRace;
        }
        public TimePoint getNow() {
            return now;
        }
        public Waypoint getStartWaypoint() {
            return startWaypoint;
        }
        public Competitor getCompetitor() {
            return competitor;
        }
        public TrackedRace getTrackedRace() {
            return trackedRace;
        }
    }

    protected InsertRaceStatsStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"RaceStats\" (\"race\", \"regatta\", \"competitorId\", \"rankOneBased\", \"distanceSailedInMeters\", \"elapsedTimeInSeconds\", "+
                        "\"avgCrossTrackErrorInMeters\", \"absoluteAvgCrossTrackErrorInMeters\", \"startDelayInSeconds\", \"distanceFromStartLineInMetersAtStart\", "+
                        "\"windwardDistanceFromStartLineInMetersAtStart\", \"speedWhenCrossingStartLineInKnots\", \"startTack\", \"rank90sAfterStart\") "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(TrackedRaceWithCompetitorAndStartWaypoint raceColumnFleetAndTrackedRace) throws SQLException {
        final TrackedRace trackedRace = raceColumnFleetAndTrackedRace.getTrackedRace();
        final Competitor competitor = raceColumnFleetAndTrackedRace.getCompetitor();
        final TimePoint now = raceColumnFleetAndTrackedRace.getNow();
        getPreparedStatement().setString(1, trackedRace.getRace().getName());
        getPreparedStatement().setString(2, trackedRace.getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setString(3, competitor.getId().toString());
        getPreparedStatement().setObject(4 , trackedRace.getRank(competitor, now));
        setDouble(5, metersOr0ForNull(trackedRace.getDistanceTraveled(competitor, now)));
        setDouble(6, secondsOr0ForNull(trackedRace.getTimeSailedSinceRaceStart(competitor, now)));
        setDouble(7, metersOr0ForNull(trackedRace.getAverageSignedCrossTrackError(competitor, now, /* waitForLatest */ false)));
        setDouble(8, metersOr0ForNull(trackedRace.getAverageAbsoluteCrossTrackError(competitor, now, /* waitForLatest */ false)));
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        final double startDelayInSeconds;
        Tack startTack;
        final boolean didCompetitorStart;
        if (raceColumnFleetAndTrackedRace.getStartWaypoint() != null && startOfRace != null) {
            NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
            trackedRace.lockForRead(competitorMarkPassings);
            try {
                if (!Util.isEmpty(competitorMarkPassings)) {
                    final MarkPassing competitorStartMarkPassing = competitorMarkPassings.iterator().next();
                    final TimePoint competitorStartTime = competitorStartMarkPassing.getTimePoint();
                    didCompetitorStart = competitorStartTime != null;
                    startDelayInSeconds = secondsOr0ForNull(startOfRace.until(competitorStartTime));
                    try {
                        startTack = trackedRace.getTack(competitor, competitorStartTime);
                    } catch (NoWindException e) {
                        startTack = null;
                    }
                } else {
                    startDelayInSeconds = 0;
                    startTack = null;
                    didCompetitorStart = false;
                }
            } finally {
                trackedRace.unlockAfterRead(competitorMarkPassings);
            }
        } else {
            startDelayInSeconds = 0;
            startTack = null;
            didCompetitorStart = false;
        }
        setDouble(9, startDelayInSeconds);
        if (startOfRace != null && didCompetitorStart) {
            setDouble(10, metersOr0ForNull(trackedRace.getDistanceToStartLine(competitor, startOfRace)));
            setDouble(11, metersOr0ForNull(trackedRace.getWindwardDistanceToFavoredSideOfStartLine(competitor, startOfRace)));
            final Speed speedWhenCrossingStartLine = trackedRace.getSpeedWhenCrossingStartLine(competitor);
            setDouble(12, speedWhenCrossingStartLine==null?0:speedWhenCrossingStartLine.getKnots());
            getPreparedStatement().setString(13, startTack==null?null:startTack.name());
            getPreparedStatement().setObject(14, trackedRace.getRank(competitor, startOfRace.plus(DURATION_AFTER_START_TO_DECIDE_START_WINNER)));
        } else {
            setDouble(10, 0);
            setDouble(11, 0);
            setDouble(12, 0);
            getPreparedStatement().setString(13, null);
            getPreparedStatement().setInt(14, 0);
        }
    }
}
