package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRaceStatsStatement.TrackedRaceWithCompetitorAndStartWaypoint;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class InsertRaceStatsStatement extends AbstractPreparedInsertStatement<TrackedRaceWithCompetitorAndStartWaypoint> {
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
                        "\"avgCrossTrackErrorInMeters\", \"absoluteAvgCrossTrackErrorInMeters\", \"numberOfTacks\", "+
                        "\"numberOfGybes\", \"numberOfPenaltyCircles\", \"startDelayInSeconds\", \"distanceFromStartLineInMetersAtStart\", "+
                        "\"speedWhenCrossingStartLineInKnots\", \"startTack\") "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(TrackedRaceWithCompetitorAndStartWaypoint raceColumnFleetAndTrackedRace) throws SQLException {
        final TrackedRace trackedRace = raceColumnFleetAndTrackedRace.getTrackedRace();
        final Competitor competitor = raceColumnFleetAndTrackedRace.getCompetitor();
        final TimePoint now = raceColumnFleetAndTrackedRace.getNow();
        getPreparedStatement().setString(1, trackedRace.getRace().getName());
        getPreparedStatement().setString(2, trackedRace.getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setString(3, competitor.getId().toString());
        getPreparedStatement().setInt(4, trackedRace.getRank(competitor, now));
        setDouble(5, metersOr0ForNull(trackedRace.getDistanceTraveled(competitor, now)));
        setDouble(6, secondsOr0ForNull(trackedRace.getTimeSailedSinceRaceStart(competitor, now)));
        setDouble(7, metersOr0ForNull(trackedRace.getAverageSignedCrossTrackError(competitor, now, /* waitForLatest */ false)));
        setDouble(8, metersOr0ForNull(trackedRace.getAverageAbsoluteCrossTrackError(competitor, now, /* waitForLatest */ false)));
        final Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, /* waitForLatest */ false);
        getPreparedStatement().setInt(9, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.TACK)));
        getPreparedStatement().setInt(10, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.JIBE)));
        getPreparedStatement().setInt(11, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.PENALTY_CIRCLE)));
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        final double startDelay;
        Tack startTack;
        if (raceColumnFleetAndTrackedRace.getStartWaypoint() != null && startOfRace != null) {
            NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
            trackedRace.lockForRead(competitorMarkPassings);
            try {
                if (!Util.isEmpty(competitorMarkPassings)) {
                    final MarkPassing competitorStartMarkPassing = competitorMarkPassings.iterator().next();
                    final TimePoint competitorStartTime = competitorStartMarkPassing.getTimePoint();
                    startDelay = secondsOr0ForNull(startOfRace.until(competitorStartTime));
                    try {
                        startTack = trackedRace.getTack(competitor, competitorStartTime);
                    } catch (NoWindException e) {
                        startTack = null;
                    }
                } else {
                    startDelay = 0;
                    startTack = null;
                }
            } finally {
                trackedRace.unlockAfterRead(competitorMarkPassings);
            }
        } else {
            startDelay = 0;
            startTack = null;
        }
        setDouble(12, startDelay);
        if (startOfRace != null) {
            setDouble(13, metersOr0ForNull(trackedRace.getDistanceToStartLine(competitor, startOfRace)));
            final Speed speedWhenCrossingStartLine = trackedRace.getSpeedWhenCrossingStartLine(competitor);
            setDouble(14, speedWhenCrossingStartLine==null?0:speedWhenCrossingStartLine.getKnots());
            getPreparedStatement().setString(15, startTack==null?null:startTack.name());
        } else {
            setDouble(13, 0);
            setDouble(14, 0);
        }
    }
}
