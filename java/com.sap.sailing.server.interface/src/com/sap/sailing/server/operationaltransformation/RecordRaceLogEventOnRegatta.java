package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RecordRaceLogEventOnRegatta extends AbstractRaceLogOnRegattaOperation<RaceLogEvent> {
    private static final long serialVersionUID = 8092146834280389864L;
    private final RaceLogEvent event;
    
    public RecordRaceLogEventOnRegatta(String regattaName, String raceColumnName, 
            String fleetName, RaceLogEvent event) {
        super(regattaName, raceColumnName, fleetName);
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
