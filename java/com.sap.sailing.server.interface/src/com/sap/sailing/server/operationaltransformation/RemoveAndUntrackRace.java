package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RemoveAndUntrackRace extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 4260421466093529004L;

    public RemoveAndUntrackRace(RegattaAndRaceIdentifier raceIdentifier) {
        super(raceIdentifier);
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
     * {@link #internalApplyTo(RacingEventService)} does not replicate the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return true;
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) {
        Regatta regatta = toState.getRegatta(getRaceIdentifier());
        if (regatta != null) {
            RaceDefinition race = regatta.getRaceByName(getRaceIdentifier().getRaceName());
            if (race != null) {
                try {
                    toState.removeRace(regatta, race);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
