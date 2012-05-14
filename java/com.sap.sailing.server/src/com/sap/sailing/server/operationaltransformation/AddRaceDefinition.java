package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddRaceDefinition extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -2282997511967012426L;
    private final RegattaIdentifier eventIdentifier;
    private final RaceDefinition raceDefinition;
    
    public AddRaceDefinition(RegattaIdentifier eventIdentifier, RaceDefinition raceDefinition) {
        super();
        this.eventIdentifier = eventIdentifier;
        this.raceDefinition = raceDefinition;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.addRace(eventIdentifier, raceDefinition);
        return null;
    }

}
