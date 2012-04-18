package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.server.RacingEventService;

public class AddRaceDefinition extends AbstractRacingEventServiceOperation {
    private static final long serialVersionUID = -2282997511967012426L;
    private final EventIdentifier eventIdentifier;
    private final RaceDefinition raceDefinition;
    
    public AddRaceDefinition(EventIdentifier eventIdentifier, RaceDefinition raceDefinition) {
        super();
        this.eventIdentifier = eventIdentifier;
        this.raceDefinition = raceDefinition;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.addRace(eventIdentifier, raceDefinition);
        return toState;
    }

}
