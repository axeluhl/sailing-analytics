package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.RaceDefinition;

/**
 * Base class for all {@link RaceTracker}s that must implement listener notifications
 */
public abstract class AbstractRaceTrackerBaseImpl implements RaceTracker {
    private final RaceTrackerListeners listeners = new RaceTrackerListeners();
    private final Set<RaceTracker.RaceCreationListener> raceCreationListeners = Collections
            .newSetFromMap(new ConcurrentHashMap<RaceTracker.RaceCreationListener, Boolean>());
    
    private final RaceTrackingConnectivityParameters connectivityParams;

    public AbstractRaceTrackerBaseImpl(RaceTrackingConnectivityParameters connectivityParams) {
        super();
        this.connectivityParams = connectivityParams;
    }

    /**
     * Ensure stop method does notify all listeners after tracker stopped.
     */
    @Override
    public final void stop(boolean preemptive) throws MalformedURLException, IOException, InterruptedException {
        this.stop(preemptive, /* willBeRemoved */ false);
    }

    /**
     * Ensure stop method does notify all listeners after tracker stopped. If the race will be removed afterwards
     * by the caller, the caller should pass {@code true} for the {@code willBeRemoved} parameter. This will, in
     * particular, avoid that unnecessary re-calculations will be triggered.
     */
    @Override
    public final void stop(boolean preemptive, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException {
        try {
            listeners.onTrackerWillStop(preemptive, willBeRemoved);
        } finally {
            onStop(preemptive, willBeRemoved);
        }
    }

    /**
     * Template stop method for subclasses.
     * 
     * @param willBeRemoved
     *            If the race will be removed afterwards by the caller, the caller should pass {@code true} for the
     *            {@code willBeRemoved} parameter. This will, in particular, avoid that unnecessary re-calculations will
     *            be triggered.
     */
    protected void onStop(boolean preemptive, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException {
    }

    @Override
    public boolean add(Listener listener) {
        return listeners.addListener(listener);
    }

    @Override
    public void remove(Listener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public void add(RaceCreationListener listener) {
        final RaceDefinition race;
        synchronized (raceCreationListeners) {
            race = getRace();
            if (race == null) {
                raceCreationListeners.add(listener);
            }
        }
        if (race != null) { // listener hasn't been added above; notify immediately
            listener.onRaceCreated(this);
        }
    }

    @Override
    public void remove(RaceCreationListener listener) {
        synchronized (raceCreationListeners) {
            raceCreationListeners.remove(listener);
        }
    }
    
    /**
     * Notifies all {@link RaceCreationListener}s registered and removes them. Must be called
     * after {@link #getRace()} has started returning a valid, non-{@code null} race.
     */
    protected void notifyRaceCreationListeners() {
        assert getRace() != null;
        final Set<RaceCreationListener> listenersToNotify;
        synchronized (raceCreationListeners) {
            listenersToNotify = new HashSet<>(raceCreationListeners);
            raceCreationListeners.clear();
        }
        listenersToNotify.forEach(l->l.onRaceCreated(this));
    }

    @Override
    public RaceTrackingConnectivityParameters getConnectivityParams() {
        return connectivityParams;
    }
}
