package com.sap.sailing.domain.tracking;

/**
 * Listener to be informed about new {@link TrackedRegatta}s or ones that are removed.
 * 
 * This listener can not directly be registered on RacingEventService because this would potentially lead to OSGi
 * bundles that could not be stopped/updated. Instead you must publish your listener instance to the OSGi service
 * registry as {@link TrackedRegattaListener} to make it obtainable by RacingEventService. Your published listener will
 * automatically be called on {@link TrackedRegatta} changes.
 */
public interface TrackedRegattaListener {
    void regattaAdded(TrackedRegatta trackedRegatta);

    void regattaRemoved(TrackedRegatta trackedRegatta);
}
