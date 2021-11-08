package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractRaceLogOnLeaderboardOperation<T> extends AbstractRaceLogOperation<T> {
    private static final long serialVersionUID = -5649159437859782663L;
    private final String leaderboardName;
    
    public AbstractRaceLogOnLeaderboardOperation(String leaderboardName, String raceColumnName, String fleetName) {
        super(raceColumnName, fleetName);
        this.leaderboardName = leaderboardName;
    }

    protected RaceColumn getRaceColumn(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(leaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(getRaceColumnName());
        return raceColumn;
    }

}
