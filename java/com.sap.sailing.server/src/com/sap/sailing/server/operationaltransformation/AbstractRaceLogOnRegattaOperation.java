package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractRaceLogOnRegattaOperation extends AbstractRaceLogOperation {
    private static final long serialVersionUID = -7174569054572099191L;
    private final String regattaName;
    
    public AbstractRaceLogOnRegattaOperation(String regattaName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(raceColumnName, fleetName, event);
        this.regattaName = regattaName;
    }

    protected String getRegattaName() {
        return regattaName;
    }

    @Override
    protected RaceColumn getRaceColumn(RacingEventService toState) {
        Regatta regatta = toState.getRegattaByName(regattaName);
        RaceColumn raceColumn = null;
        for (Series series : regatta.getSeries()) {
            raceColumn = series.getRaceColumnByName(getRaceColumnName());
            if (raceColumn != null) {
                break;
            }
        }
        return raceColumn;
    }

}
