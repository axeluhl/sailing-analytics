package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.leaderboard.ScoringScheme;

public class InsertScoringSchemeStatement extends AbstractPreparedInsertStatement<ScoringScheme> {
    protected InsertScoringSchemeStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"ScoringScheme\" (\"id\", \"higherIsBetter\") VALUES (?, ?);"));
    }

    @Override
    public void parameterizeStatement(ScoringScheme scoringScheme) throws SQLException {
        getPreparedStatement().setString(1, scoringScheme.getType().name());
        getPreparedStatement().setBoolean(2, scoringScheme.isHigherBetter());
    }
}
