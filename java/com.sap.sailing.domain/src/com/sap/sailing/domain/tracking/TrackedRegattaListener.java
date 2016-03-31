package com.sap.sailing.domain.tracking;

public interface TrackedRegattaListener {
    void regattaAdded(TrackedRegatta trackedRegatta);

    void regattaRemoved(TrackedRegatta trackedRegatta);
}
