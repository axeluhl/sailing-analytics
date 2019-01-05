package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractRaceLogOnRegattaOperation<T> extends AbstractRaceLogOperation<T> {
    private static final long serialVersionUID = -7174569054572099191L;
    private final String regattaName;
    
    public AbstractRaceLogOnRegattaOperation(String regattaName, String raceColumnName, 
            String fleetName) {
        super(raceColumnName, fleetName);
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
