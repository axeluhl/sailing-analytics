package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Distance;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverLoss {

    private final Distance distanceSailed;
    private final Distance distanceSailedIfNotManeuvering;

    public ManeuverLoss(Distance distanceSailed, Distance distanceSailedIfNotManeuvering) {
        this.distanceSailed = distanceSailed;
        this.distanceSailedIfNotManeuvering = distanceSailedIfNotManeuvering;
    }

    public Distance getDistanceSailed() {
        return distanceSailed;
    }

    public Distance getDistanceSailedIfNotManeuvering() {
        return distanceSailedIfNotManeuvering;
    }
    
    public Distance getDistanceLost() {
        return distanceSailedIfNotManeuvering.add(distanceSailed.scale(-1));
    }
    
    /**
     * Gets the ratio between {@link #getDistanceSailed()} and
     * {@link #getDistanceSailedIfNotManeuvering()}.
     */
    public double getRatioBetweenDistanceSailedWithAndWithoutManeuver() {
        return getDistanceSailed().getMeters() / getDistanceSailedIfNotManeuvering().getMeters();
    }

}
