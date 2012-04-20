package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.server.RacingEventService;

public class StopTrackingRace extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -6074168525544219596L;

    public StopTrackingRace(EventAndRaceIdentifier raceIdentifier) {
        super(raceIdentifier);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Event event = toState.getEvent(getRaceIdentifier());
        if (event != null) {
            RaceDefinition r = toState.getRace(getRaceIdentifier());
            if (r != null) {
                toState.stopTracking(event, r);
            }
        }
        return null;
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

}
