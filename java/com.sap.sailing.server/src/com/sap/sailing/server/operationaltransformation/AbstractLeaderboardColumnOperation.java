package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;



public abstract class AbstractLeaderboardColumnOperation<ResultType> extends AbstractLeaderboardOperation<ResultType> {
    private static final long serialVersionUID = 8577267743945864388L;
    private final String columnName;
    
    public AbstractLeaderboardColumnOperation(String leaderboardName, String columnName) {
        super(leaderboardName);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
    
    protected boolean affectsSameColumn(AbstractLeaderboardColumnOperation<?> other) {
        return affectsSameLeaderboard(other) && getColumnName().equals(other.getColumnName());
    }

    /**
     * If the column to update belongs to a {@link FlexibleLeaderboard}, it's that leaderboard which needs to be
     * updated in the persistent store. Otherwise, the race column has to be a {@link RaceColumnInSeries} whose
     * regatta needs to be updated.
     */
    protected void updateDB(RacingEventService toState, Leaderboard leaderboard, RaceColumn raceColumn) {
        if (raceColumn instanceof RaceColumnInSeries) {
            toState.updateStoredRegatta(((RaceColumnInSeries) raceColumn).getSeries().getRegatta());
        } else {
            toState.updateStoredLeaderboard(leaderboard);
        }
    }

}
