package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplateResolver;
import com.sap.sailing.domain.racelog.impl.RaceLogOnLeaderboardIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;
import com.sap.sailing.server.RacingEventService;

public class RaceLogLoadedOnRegatta extends AbstractRaceLogOnRegattaOperation<Void> {
    private static final long serialVersionUID = -424130161463117765L;
    private final RaceLog raceLog;

    public RaceLogLoadedOnRegatta(String regattaName, String raceColumnName, String fleetName, RaceLog raceLog) {
        super(regattaName, raceColumnName, fleetName);
        this.raceLog = raceLog;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        final RaceColumn raceColumn = getRaceColumn(toState);
        raceColumn.setOrReloadRaceLogInformation(, raceColumn.getFleetByName(getFleetName()));
        RaceLog oldRaceLog = raceColumn.getRaceLog(raceColumn.getFleetByName(getFleetName()));
        
        oldRaceLog.
    }
}
