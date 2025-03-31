package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.hanaexport.jaxrs.api.InsertLegStatement.TrackedLegAndNow;
import com.sap.sse.common.TimePoint;

public class InsertLegStatement extends AbstractPreparedInsertStatement<TrackedLegAndNow> {
    static class TrackedLegAndNow {
        private final TimePoint now;
        private final TrackedLeg trackedLeg;
        public TrackedLegAndNow(TimePoint now, TrackedLeg trackedLeg) {
            super();
            this.now = now;
            this.trackedLeg = trackedLeg;
        }
        public TimePoint getNow() {
            return now;
        }
        public TrackedLeg getTrackedLeg() {
            return trackedLeg;
        }
    }
    
    protected InsertLegStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"Leg\" (\"race\", \"regatta\", \"number\", \"type\") "+
                "VALUES (?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(TrackedLegAndNow trackedLegAndNow) throws SQLException {
        getPreparedStatement().setString(1, trackedLegAndNow.getTrackedLeg().getTrackedRace().getRace().getName());
        getPreparedStatement().setString(2, trackedLegAndNow.getTrackedLeg().getTrackedRace().getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setInt(3, trackedLegAndNow.getTrackedLeg().getLeg().getZeroBasedIndexOfStartWaypoint());
        final LegType legType;
        try {
            legType = trackedLegAndNow.getTrackedLeg().getLegType(trackedLegAndNow.getNow());
            getPreparedStatement().setString(4, legType.name());
        } catch (NoWindException nwe) {
            getPreparedStatement().setString(4, null);
        }

    }
}
