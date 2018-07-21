package com.sap.sailing.domain.tracking;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class that handles listener registration and notification for {@link RaceTracker}. If the underlying
 * {@link RaceTracker} is already stopped, the listener will immediately be notified by
 * {@link RaceTracker.Listener#onTrackerWillStop(boolean preemptive, boolean willBeRemoved)}
 */
public class RaceTrackerListeners implements RaceTracker.Listener {
    private final HashSet<RaceTracker.Listener> registeredListeners = new HashSet<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean isStoppedPreemptive = new AtomicBoolean(false);

    public synchronized boolean addListener(final RaceTracker.Listener listener) {
        if (isStopped.get()) {
            return false;
        } else {
            registeredListeners.add(listener);
            return true;
        }
    }

    public synchronized void removeListener(final RaceTracker.Listener listener) {
        registeredListeners.remove(listener);
    }

    @Override
    public void onTrackerWillStop(boolean preemptive, boolean willBeRemoved) {
        Iterable<RaceTracker.Listener> listenersToInform;
        synchronized (this) {
            isStopped.set(true);
            isStoppedPreemptive.set(preemptive);
            listenersToInform = new HashSet<>(registeredListeners);
        }
        listenersToInform.forEach(l -> l.onTrackerWillStop(preemptive, willBeRemoved));
    }
}
