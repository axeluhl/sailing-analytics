package com.sap.sailing.domain.tracking;

public enum EmptyTrackedRegattaListener implements TrackedRegattaListener {
    INSTANCE;

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
    }
}
