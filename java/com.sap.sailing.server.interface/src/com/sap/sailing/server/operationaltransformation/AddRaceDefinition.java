package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class AddRaceDefinition extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -2282997511967012426L;
    private final RegattaIdentifier regattaIdentifier;
    private final RaceDefinition raceDefinition;
    
    public AddRaceDefinition(RegattaIdentifier regattaIdentifier, RaceDefinition raceDefinition) {
        super();
        this.regattaIdentifier = regattaIdentifier;
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

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.addRace(regattaIdentifier, raceDefinition);
        return null;
    }

}
