package com.sap.sailing.domain.common.tracking;

/**
 * Identifies the tracking technology used for a TrackedRace.
 * 
 * TODO This must go away. We paid great attention keeping the domain independent of connectors. This enumeration type kills that whole idea.
 */
public interface TrackingConnectorType {
    String name();
    
    default String getDefaultUrl() {
        return null;
    }
}
