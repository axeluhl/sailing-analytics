package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public class HashCalculationForTrackedRaceHashImpl {
    private final TrackedRaceImpl trackedRace;
    
    public HashCalculationForTrackedRaceHashImpl (TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
    }
    
    public int calculateHashForWaypoints (Iterable<Waypoint> waypoints) {
        int res = 0;
        for(Waypoint w : waypoints) {
            res = res ^ w.getName().hashCode();
            // We could also include further information, such as marks, but it would slow down the computation
        }
        res = res * 31;
        return res;
    }
    
    public int calculateHashForWaypoint (Waypoint w) {
        int res = 0;
        res = res ^ w.getName().hashCode();
        res = res * 31;
            // We could also include further information, such as marks, but it would slow down the computation
        return res;
    }
    
    public int calculateHashForMarkpassings (NavigableSet<MarkPassing> markpassings) {
        int res = 0; 
        for(MarkPassing m : markpassings) {
            res = res ^ calculateHashForWaypoint(m.getWaypoint());
            //Didn't include competitor and original Markpassing, since I think that they aren't Important and consume calculating power
        }
        return res;
    }
    
    public int calculateHashForBoat (Boat b) {
        int res = 0; 
        res = res ^ b.getSailID().hashCode();
        //res = res ^ b.getName().hashCode(); -> null pointer 
        res = res * 31;
        return res; 
    }
}
