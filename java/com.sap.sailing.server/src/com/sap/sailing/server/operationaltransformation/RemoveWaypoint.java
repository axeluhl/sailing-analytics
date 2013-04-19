package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveWaypoint extends WaypointOperation {
    private static final long serialVersionUID = 7461197139000309999L;

    public RemoveWaypoint(RegattaAndRaceIdentifier raceIdentifier, int zeroBasedIndex, Waypoint waypoint) {
        super(raceIdentifier, zeroBasedIndex, waypoint);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        RaceDefinition race = toState.getRace(getRaceIdentifier());
        race.getCourse().removeWaypoint(getZeroBasedIndex());
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
