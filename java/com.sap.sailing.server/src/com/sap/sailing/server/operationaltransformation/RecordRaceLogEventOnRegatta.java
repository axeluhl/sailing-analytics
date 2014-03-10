package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;

public class RecordRaceLogEventOnRegatta extends AbstractRaceLogOnRegattaOperation {
    private static final long serialVersionUID = 8092146834280389864L;
    
    public RecordRaceLogEventOnRegatta(String regattaName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(regattaName, raceColumnName, fleetName, event);
    }

    @Override
    protected RaceColumn getRaceColumn(RacingEventService toState) {
        Regatta regatta = toState.getRegattaByName(getRegattaName());
        RaceColumn raceColumn = null;
        for (Series series : regatta.getSeries()) {
            raceColumn = series.getRaceColumnByName(getRaceColumnName());
            if (raceColumn != null) {
                break;
            }
        }
        return raceColumn;
    }

    @Override
    public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        RaceColumn raceColumn = getRaceColumn(toState);
        return new RaceLogEventRecorder(getFleetName(), getEvent()).addEventTo(raceColumn);
    }

}
