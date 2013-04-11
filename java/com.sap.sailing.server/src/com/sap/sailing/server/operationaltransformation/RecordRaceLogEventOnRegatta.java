package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEventOnRegatta extends AbstractRecordRaceLogEvent {
    private static final long serialVersionUID = 8092146834280389864L;

    private final String regattaName;
    
    public RecordRaceLogEventOnRegatta(String regattaName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(raceColumnName, fleetName, event);
        this.regattaName = regattaName;
    }

    @Override
    public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.getRegattaByName(regattaName);
        RaceColumn raceColumn = null;
        for (Series series : regatta.getSeries()) {
            raceColumn = series.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                break;
            }
        }
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
