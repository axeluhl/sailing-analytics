package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.server.RacingEventService;

public class RecordRaceLogEventOnLeaderboard extends AbstractRaceLogOnLeaderboardOperation<RaceLogEvent> {
    private static final long serialVersionUID = 1008913415922358665L;
    private final RaceLogEvent event;

    public RecordRaceLogEventOnLeaderboard(String leaderboardName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(leaderboardName, raceColumnName, fleetName);
        this.event = event;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        RaceColumn raceColumn = getRaceColumn(toState);
        return new RaceLogEventRecorder(getFleetName(), event).addEventTo(raceColumn);
    }
}
