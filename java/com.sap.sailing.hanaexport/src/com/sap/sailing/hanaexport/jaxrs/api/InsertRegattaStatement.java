package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRegattaStatement.RegattaAndEvent;

public class InsertRegattaStatement extends AbstractPreparedInsertStatement<RegattaAndEvent> {
    static class RegattaAndEvent {
        private final Regatta regatta;
        private final Event event;
        public RegattaAndEvent(Regatta regatta, Event event) {
            super();
            this.regatta = regatta;
            this.event = event;
        }
        public Regatta getRegatta() {
            return regatta;
        }
        public Event getEvent() {
            return event;
        }
    }
    
    protected InsertRegattaStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"Regatta\" (\"name\", \"boatClass\", \"scoringScheme\", \"rankingMetric\", \"eventId\") "+
                "VALUES (?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(RegattaAndEvent regattaAndEvent) throws SQLException {
        final Regatta regatta = regattaAndEvent.getRegatta();
        final Event event = regattaAndEvent.getEvent();
        getPreparedStatement().setString(1, regatta.getName());
        getPreparedStatement().setString(2, regatta.getBoatClass().getName());
        getPreparedStatement().setString(3, regatta.getScoringScheme().getType().name());
        getPreparedStatement().setString(4, regatta.getRankingMetricType().name());
        getPreparedStatement().setString(5, event == null ? null : event.getId().toString());
    }
}
