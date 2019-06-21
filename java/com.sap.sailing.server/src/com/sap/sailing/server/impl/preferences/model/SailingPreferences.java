package com.sap.sailing.server.impl.preferences.model;

/**
 * Defines common prefixes for classes of preferences in SAP Sailing to ensure some level of consistency when defining
 * preference keys.
 */
public interface SailingPreferences {
    public static final String NOTIFICATION_PREFERENCES_PREFIX = "sailing.notifications.";
    public static final String STORED_DATAMINING_QUERY_PREFERENCES = "sailing.datamining.storedqueries";
    public static final String TRACKED_EVENTS_PREFERENCES = "sailing.profile.trackedevents";
}
