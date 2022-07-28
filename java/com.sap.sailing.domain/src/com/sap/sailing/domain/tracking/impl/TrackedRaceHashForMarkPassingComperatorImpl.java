package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Waypoint;

public class TrackedRaceHashForMarkPassingComperatorImpl {

    enum typeOfHash {
        COMPETITOR,
        BOAT,
        START,
        END,
        WAYPOINTS,
        NUMBEROFGPSFIXES,
        GPSFIXES
      }

    private TrackedRaceImpl trackedRace;
    private final Iterable<Waypoint> waypoints;
    
    public TrackedRaceHashForMarkPassingComperatorImpl (TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
        this.waypoints = trackedRace.getRace().getCourse().getWaypoints();
    }
    
}
