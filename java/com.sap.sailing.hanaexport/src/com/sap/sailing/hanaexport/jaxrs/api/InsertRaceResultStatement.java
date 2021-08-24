package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRaceResultStatement.RaceResult;
import com.sap.sse.common.TimePoint;

public class InsertRaceResultStatement extends AbstractPreparedInsertStatement<RaceResult> {
    static class RaceResult {
        private final Regatta regatta;
        private final Leaderboard leaderboard;
        private final Competitor competitor;
        private final RaceColumn raceColumn;
        private final TimePoint now;
        public RaceResult(Regatta regatta, Leaderboard leaderboard, Competitor competitor, RaceColumn raceColumn,
                TimePoint now) {
            super();
            this.regatta = regatta;
            this.leaderboard = leaderboard;
            this.competitor = competitor;
            this.raceColumn = raceColumn;
            this.now = now;
        }
        public Regatta getRegatta() {
            return regatta;
        }
        public Leaderboard getLeaderboard() {
            return leaderboard;
        }
        public Competitor getCompetitor() {
            return competitor;
        }
        public RaceColumn getRaceColumn() {
            return raceColumn;
        }
        public TimePoint getNow() {
            return now;
        }
    }
    
    protected InsertRaceResultStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"RaceResult\" (\"regatta\", \"raceColumn\", \"competitorId\", \"points\", "+
                        "\"discarded\", \"irm\") "+
                        "VALUES (?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(RaceResult raceResult) throws SQLException {
        getPreparedStatement().setString(1, raceResult.getRegatta().getName());
        getPreparedStatement().setString(2, raceResult.getRaceColumn().getName());
        getPreparedStatement().setString(3, raceResult.getCompetitor().getId().toString());
        final Double totalPoints = raceResult.getLeaderboard().getTotalPoints(raceResult.getCompetitor(), raceResult.getRaceColumn(), raceResult.getNow());
        setDouble(4, totalPoints == null ? 0 : totalPoints);
        getPreparedStatement().setBoolean(5, raceResult.getLeaderboard().isDiscarded(raceResult.getCompetitor(), raceResult.getRaceColumn(), raceResult.getNow()));
        final MaxPointsReason maxPointsReason = raceResult.getLeaderboard().getMaxPointsReason(raceResult.getCompetitor(), raceResult.getRaceColumn(), raceResult.getNow());
        getPreparedStatement().setString(6, (maxPointsReason == null ? MaxPointsReason.NONE : maxPointsReason).name());
    }
}
