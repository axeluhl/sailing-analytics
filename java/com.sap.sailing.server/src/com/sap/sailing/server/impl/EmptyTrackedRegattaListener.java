package com.sap.sailing.server.impl;

import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;

/**
 * Empty implementation of {@link TrackedRegattaListener} e.g. used by tests.
 */
public enum EmptyTrackedRegattaListener implements TrackedRegattaListenerManager {
    INSTANCE;

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
    }

    @Override
    public void addListener(TrackedRegattaListener listener) {
    }

    @Override
    public void removeListener(TrackedRegattaListener listener) {
    }
}
