package com.sap.sailing.domain.common.tracking;

/**
 * Identifies the tracking technology used for a TrackedRace.
 */
public enum TrackingConnectorType {
    TracTrac("https://www.tractrac.com/"), SwissTiming(null), RaceLog(null);

    private final String defaultUrl;

    private TrackingConnectorType(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }
}
