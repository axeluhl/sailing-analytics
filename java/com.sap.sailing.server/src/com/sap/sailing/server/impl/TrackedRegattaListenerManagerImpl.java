package com.sap.sailing.server.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;

/**
 * Default implementation of {@link TrackedRegattaListenerManager} having a local set of {@link TrackedRegattaListener}.
 */
public class TrackedRegattaListenerManagerImpl implements TrackedRegattaListenerManager {

    private final Set<TrackedRegattaListener> listeners;

    public TrackedRegattaListenerManagerImpl() {
        listeners = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
        for (TrackedRegattaListener trackedRegattaListener : new HashSet<>(listeners)) {
            trackedRegattaListener.regattaAdded(trackedRegatta);
        }
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
        for (TrackedRegattaListener trackedRegattaListener : new HashSet<>(listeners)) {
            trackedRegattaListener.regattaRemoved(trackedRegatta);
        }
    }

    @Override
    public void addListener(TrackedRegattaListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(TrackedRegattaListener listener) {
        listeners.remove(listener);
    }
}
