package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRaceStatement.TrackedRaceWithRaceColumnAndFleet;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class InsertRaceStatement extends AbstractPreparedInsertStatement<TrackedRaceWithRaceColumnAndFleet> {
    static class TrackedRaceWithRaceColumnAndFleet {
        private final TrackedRace trackedRace;
        private final RaceColumn raceColumn;
        private final Fleet fleet;
        public TrackedRaceWithRaceColumnAndFleet(TrackedRace trackedRace, RaceColumn raceColumn, Fleet fleet) {
            super();
            this.trackedRace = trackedRace;
            this.raceColumn = raceColumn;
            this.fleet = fleet;
        }
        public TrackedRace getTrackedRace() {
            return trackedRace;
        }
        public RaceColumn getRaceColumn() {
            return raceColumn;
        }
        public Fleet getFleet() {
            return fleet;
        }
    }
    
    protected InsertRaceStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"Race\" (\"name\", \"regatta\", \"raceColumn\", \"fleet\", \"startOfTracking\", "+
                        "\"startOfRace\", \"endOfTracking\", \"endOfRace\", \"avgWindSpeedInKnots\", \"raceColumnIndexZeroBased\", "+
                        "\"gateStart\") "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(TrackedRaceWithRaceColumnAndFleet raceColumnFleetAndTrackedRace) throws SQLException {
        final TrackedRace trackedRace = raceColumnFleetAndTrackedRace.getTrackedRace();
        getPreparedStatement().setString(1, trackedRace.getRace().getName());
        getPreparedStatement().setString(2, trackedRace.getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setString(3, raceColumnFleetAndTrackedRace.getRaceColumn().getName());
        getPreparedStatement().setString(4, raceColumnFleetAndTrackedRace.getFleet().getName());
        if (trackedRace.getStartOfTracking() != null) {
            getPreparedStatement().setDate(5, new Date(trackedRace.getStartOfTracking().asMillis()));
        } else {
            getPreparedStatement().setDate(5, null);
        }
        if (trackedRace.getStartOfRace() != null) {
            getPreparedStatement().setDate(6, new Date(trackedRace.getStartOfRace().asMillis()));
        } else {
            getPreparedStatement().setDate(6, null);
        }
        if (trackedRace.getEndOfTracking() != null) {
            getPreparedStatement().setDate(7, new Date(trackedRace.getEndOfTracking().asMillis()));
        } else {
            getPreparedStatement().setDate(7, null);
        }
        if (trackedRace.getEndOfRace() != null) {
            getPreparedStatement().setDate(8, new Date(trackedRace.getEndOfRace().asMillis()));
        } else {
            getPreparedStatement().setDate(8, null);
        }
        final SpeedWithConfidence<TimePoint> averageWind = trackedRace.getAverageWindSpeedWithConfidenceWithNumberOfSamples(/* number of samples */ 5);
        if (averageWind != null) {
            setDouble(9, averageWind.getObject().getKnots());
        } else {
            setDouble(9, 0.0);
        }
        getPreparedStatement().setInt(10, Util.indexOf(trackedRace.getTrackedRegatta().getRegatta().getRaceColumns(), raceColumnFleetAndTrackedRace.getRaceColumn()));
        final Boolean gateStart = trackedRace.isGateStart();
        getPreparedStatement().setBoolean(11, gateStart==null ? false : gateStart);
    }
}
