package com.sap.sailing.domain.maneuverdetection.impl;

public interface WindEstimationInteraction {

    /**
     * 
     * @param newManeuverSpots
     * @return {@code True} if estimated wind has considerably changed
     */
    boolean newManeuverSpotsDetected(Iterable<ManeuverSpot> newManeuverSpots);

}
