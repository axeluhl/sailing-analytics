package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class ManeuverSpot {
    
    private final Iterable<GPSFixMoving> douglasPeuckerFixes;
    private final Iterable<Maneuver> maneuvers;
    private final Iterable<Pair<TimePoint, Wind>> maneuverWinds;
    
    public ManeuverSpot(Iterable<GPSFixMoving> douglasPeuckerFixes, Iterable<Maneuver> maneuvers,
            Iterable<Pair<TimePoint, Wind>> maneuverWinds) {
        this.douglasPeuckerFixes = douglasPeuckerFixes;
        this.maneuvers = maneuvers;
        this.maneuverWinds = maneuverWinds;
    }

    public Iterable<GPSFixMoving> getDouglasPeuckerFixes() {
        return douglasPeuckerFixes;
    }

    public Iterable<Maneuver> getManeuvers() {
        return maneuvers;
    }

    public Iterable<Pair<TimePoint, Wind>> getManeuverWinds() {
        return maneuverWinds;
    }

}
