package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;

public class RecordRaceLogEventOnLeaderboard extends AbstractRaceLogOnLeaderboardOperation {
    private static final long serialVersionUID = 1008913415922358665L;

    public RecordRaceLogEventOnLeaderboard(String leaderboardName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(leaderboardName, raceColumnName, fleetName, event);
    }

    @Override
    public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        RaceColumn raceColumn = getRaceColumn(toState);
        return new RaceLogEventRecorder(getFleetName(), getEvent()).addEventTo(raceColumn);
    }
}
