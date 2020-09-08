package com.sap.sailing.domain.common.tracking;

import java.io.Serializable;

/**
 * Identifies the tracking technology used for a TrackedRace.
 */
public interface TrackingConnectorType extends Serializable {
    String name();
    
    default String getDefaultUrl() {
        return null;
    }
}
