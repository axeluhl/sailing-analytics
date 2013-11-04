package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEventOnLeaderboard extends AbstractRecordRaceLogEvent {
    private static final long serialVersionUID = 1008913415922358665L;

    private final String leaderboardName;
    
    public RecordRaceLogEventOnLeaderboard(String leaderboardName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(raceColumnName, fleetName, event);
        this.leaderboardName = leaderboardName;
    }

    @Override
    public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        Leaderboard leaderboard = toState.getLeaderboardByName(leaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        return addEventTo(raceColumn);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}
