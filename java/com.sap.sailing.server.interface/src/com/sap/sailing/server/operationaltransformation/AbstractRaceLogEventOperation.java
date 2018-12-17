package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

public abstract class AbstractRaceLogEventOperation extends AbstractRaceLogOperation<RaceLogEvent> {
    private static final long serialVersionUID = -8559301422783375526L;
    private final RaceLogEvent event;

    protected AbstractRaceLogEventOperation(String raceColumnName, String fleetName, RaceLogEvent event) {
        super(raceColumnName, fleetName);
        this.event = event;
    }

    protected RaceLogEvent getEvent() {
        return event;
    }
}
