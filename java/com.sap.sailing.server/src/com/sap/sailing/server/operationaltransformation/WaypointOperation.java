package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public abstract class WaypointOperation extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -706347390670536615L;
    private final int zeroBasedIndex;
    private final Waypoint waypoint;
    
    public WaypointOperation(RegattaAndRaceIdentifier raceIdentifier, int zeroBasedIndex, Waypoint waypoint) {
        super(raceIdentifier);
        this.zeroBasedIndex = zeroBasedIndex;
        this.waypoint = waypoint;
    }

    protected int getZeroBasedIndex() {
        return zeroBasedIndex;
    }

    protected Waypoint getWaypoint() {
        return waypoint;
    }
    
}
