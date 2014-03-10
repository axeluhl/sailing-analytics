package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public abstract class AbstractRaceLogOperation extends AbstractRacingEventServiceOperation<RaceLogEvent> {
    private static final long serialVersionUID = 2140858355670664173L;

    private final String raceColumnName;
    private final String fleetName;
    private final RaceLogEvent event;

    public AbstractRaceLogOperation(String raceColumnName, String fleetName, RaceLogEvent event) {
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.event = event;
    }
    
    protected String getRaceColumnName() {
        return raceColumnName;
    }

    protected String getFleetName() {
        return fleetName;
    }

    protected RaceLogEvent getEvent() {
        return event;
    }

    abstract protected RaceColumn getRaceColumn(RacingEventService toState);
    
    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }
}