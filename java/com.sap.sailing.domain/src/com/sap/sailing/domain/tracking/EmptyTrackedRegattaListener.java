package com.sap.sailing.domain.tracking;

/**
 * Empty implementation of {@link TrackedRegattaListener} e.g. used by tests.
 */
public enum EmptyTrackedRegattaListener implements TrackedRegattaListener {
    INSTANCE;

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
    }
}
