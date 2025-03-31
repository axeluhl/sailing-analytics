package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface RaceIdentifier extends Serializable {
    String getRegattaName();
    
    String getRaceName();
    
    Object getRace(RaceFetcher raceFetcher);

    /**
     * Immediately returns <code>null</code> if the tracked race for this race identifier doesn't exist yet.
     */
    Object getExistingTrackedRace(RaceFetcher sailingServiceImpl);
}
