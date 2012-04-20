package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.server.RacingEventService;

public class RemoveAndUntrackRace extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 4260421466093529004L;

    public RemoveAndUntrackRace(EventAndRaceIdentifier raceIdentifier) {
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

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        Event event = toState.getEvent(getRaceIdentifier());
        if (event!= null) {
            RaceDefinition race = event.getRaceByName(getRaceIdentifier().getRaceName());
            if (race != null) {
                try {
                    toState.removeRace(event, race);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
