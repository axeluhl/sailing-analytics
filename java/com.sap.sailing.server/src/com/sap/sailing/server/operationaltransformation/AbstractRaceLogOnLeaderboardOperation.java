package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractRaceLogOnLeaderboardOperation extends AbstractRaceLogOperation {
    private static final long serialVersionUID = -5649159437859782663L;
    private final String leaderboardName;
    
    public AbstractRaceLogOnLeaderboardOperation(String leaderboardName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(raceColumnName, fleetName, event);
        this.leaderboardName = leaderboardName;
    }

    protected RaceColumn getRaceColumn(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(leaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(getRaceColumnName());
        return raceColumn;
    }

}
