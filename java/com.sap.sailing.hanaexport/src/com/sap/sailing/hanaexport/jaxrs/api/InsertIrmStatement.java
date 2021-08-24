package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.common.MaxPointsReason;

public class InsertIrmStatement extends AbstractPreparedInsertStatement<MaxPointsReason> {
    protected InsertIrmStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"IRM\" (\"name\", \"discardable\", \"advanceCompetitorsTrackedWorse\", \"appliesAtStartOfRace\") VALUES (?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(MaxPointsReason irm) throws SQLException {
        getPreparedStatement().setString(1, irm.name());
        getPreparedStatement().setBoolean(2, irm.isDiscardable());
        getPreparedStatement().setBoolean(3, irm.isAdvanceCompetitorsTrackedWorse());
        getPreparedStatement().setBoolean(4, irm.isAppliesAtStartOfRace());
    }
}
