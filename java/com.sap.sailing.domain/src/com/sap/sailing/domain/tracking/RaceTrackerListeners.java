package com.sap.sailing.domain.tracking;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class that handles listener registration and notification for {@link RaceTracker}. If the underlying
 * {@link RaceTracker} is already stopped, the listener will immediately be notified by
 * {@link RaceTracker.Listener#onTrackerWillStop(boolean preemptive)}
 */
public class RaceTrackerListeners implements RaceTracker.Listener {
    private final HashSet<RaceTracker.Listener> registeredListeners = new HashSet<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean isStoppedPreemptive = new AtomicBoolean(false);

    public synchronized void addListener(final RaceTracker.Listener listener) {
        if (isStopped.get()) {
            listener.onTrackerWillStop(isStoppedPreemptive.get());
        } else {
            // if already stopped, don't bother adding to set of listeners
            registeredListeners.add(listener);
        }
    }

    public synchronized void removeListener(final RaceTracker.Listener listener) {
        registeredListeners.remove(listener);
    }

    @Override
    public synchronized void onTrackerWillStop(boolean preemptive) {
        isStopped.set(true);
        isStoppedPreemptive.set(preemptive);
        registeredListeners.forEach(l -> l.onTrackerWillStop(preemptive));
    }
}
