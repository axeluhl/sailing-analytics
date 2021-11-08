package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public abstract class AbstractRaceLogOperation<T> extends AbstractRacingEventServiceOperation<T> {
    private static final long serialVersionUID = 2140858355670664173L;

    private final String raceColumnName;
    private final String fleetName;

    public AbstractRaceLogOperation(String raceColumnName, String fleetName) {
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }
    
    protected String getRaceColumnName() {
        return raceColumnName;
    }

    protected String getFleetName() {
        return fleetName;
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