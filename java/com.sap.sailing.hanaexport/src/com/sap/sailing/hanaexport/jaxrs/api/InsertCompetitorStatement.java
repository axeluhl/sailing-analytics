package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;

public class InsertCompetitorStatement extends AbstractPreparedInsertStatement<Competitor> {
    protected InsertCompetitorStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"Competitor\" (\"id\", \"name\", \"shortName\", \"nationality\", \"sailNumber\") VALUES (?, ?, ?, ?, ?);"));
    }
    
    @Override
    public void parameterizeStatement(Competitor competitor) throws SQLException {
        getPreparedStatement().setString(1, competitor.getId().toString());
        getPreparedStatement().setString(2, competitor.getName());
        getPreparedStatement().setString(3, competitor.getShortName());
        getPreparedStatement().setString(4, competitor.getNationality() == null ? "   " : competitor.getNationality().getThreeLetterIOCAcronym());
        getPreparedStatement().setString(5, competitor.hasBoat() ? ((CompetitorWithBoat) competitor).getBoat().getSailID() : null);
    }
}
