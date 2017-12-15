package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Maneuver;

public class ManeuverSpot {
    
    private final Iterable<GPSFixMoving> douglasPeuckerFixes;
    private final NauticalSide maneuverSpotDirection;
    private final Iterable<Maneuver> maneuvers;
    
    public ManeuverSpot(Iterable<GPSFixMoving> douglasPeuckerFixes, NauticalSide maneuverSpotDirection, Iterable<Maneuver> maneuvers) {
        this.douglasPeuckerFixes = douglasPeuckerFixes;
        this.maneuvers = maneuvers;
        this.maneuverSpotDirection = maneuverSpotDirection;
    }

    public Iterable<GPSFixMoving> getDouglasPeuckerFixes() {
        return douglasPeuckerFixes;
    }
    
    public NauticalSide getManeuverSpotDirection() {
        return maneuverSpotDirection;
    }

    public Iterable<Maneuver> getManeuvers() {
        return maneuvers;
    }

}
