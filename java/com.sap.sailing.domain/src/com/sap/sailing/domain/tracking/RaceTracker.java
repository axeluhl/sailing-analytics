package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

/**
 * Centerpiece of a tracking adapter. A tracker is responsible for receiving tracking data for one
 * {@link RaceDefinition race} that is {@link Regatta#getAllRaces() part of} a common {@link #getRegatta() Event}. Some
 * tracker architectures may not be able to deliver all data for the {@link RaceDefinition} when created or started.
 * Therefore, {@link #getRace()} may return <code>null</code> if the race information hasn't been received by the
 * tracker yet. Through the {@link RaceHandle} returned by {@link #getRaceHandle()} it is also possible to perform a
 * {@link RaceHandle#getRace() blocking get} for the race tracked by this tracker.
 * <p>
 * 
 * The data received by the tracker is usually fed into {@link TrackedRace} objects that {@link TrackedRace#getRace()
 * correspond} to the {@link RaceDefinition} objects for whose tracking this tracker is responsible. When the
 * {@link TrackedRace} isn't connected to its {@link TrackedRegatta#getTrackedRaces() owning} {@link TrackedRegatta}, a
 * tracker is assumed to no longer update the {@link TrackedRace} object, even if it hasn't been {@link #stop(boolean) stopped}.
 * <p>
 * 
 * A tracker may be {@link #stop(boolean) stopped}. In this case, it will no longer receive any data at all. Stopping a tracker
 * will not modify the {@link Regatta} and the {@link TrackedRegatta} with regards to their ownership of their
 * {@link RaceDefiniion} and {@link TrackedRace}, respectively.
 * <p>
 * 
 * A {@link RaceTracker} controls the lifecycle of a {@link TrackedRace} in contrast to a {@link TrackingDataLoader} which just
 * contributes to the composite status but does not control the lifecycle.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface RaceTracker {
    /**
     * By default, wait one minute for race data; sometimes, a tracking provider's server may be under heavy load and
     * may serve races one after another. If many races are requested concurrently, this can lead to a queue of several
     * minutes length.
     */
    static long TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS = 60000;

    /**
     * Stops tracking the race.
     * 
     * @param preemptive
     *            if <code>false</code>, the tracker will continue to process data already received but will stop
     *            receiving new data. If <code>true</code>, the tracker will stop processing data immediately, ignoring
     *            (dropping) all data already received but not yet processed.
     */
    void stop(boolean preemptive) throws MalformedURLException, IOException, InterruptedException;


    /**
     * Like {@link #stop(boolean)}, only that with this method the caller can assert by setting {@code willBeRemoved} to
     * {@code true} that the race will be removed and no longer be accessible to clients after stopping this tracker.
     * This helps save computational efforts because calculations that would otherwise be triggered when loading a race
     * completes no longer need to be triggered.
     */
    void stop(boolean preemptive, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Regatta getRegatta();

    /**
     * Returns the race being tracked by this tracker, in a "volatile" way, meaning that if another thread belonging to
     * this tracker has established the {@link RaceDefinition} for this tracker, other threads will immediately see this
     * object. Non-blocking call that returns <code>null</code> if the {@link RaceDefinition} hasn't been created yet,
     * e.g., because the course definition hasn't been received yet or the listener for receiving course information
     * hasn't been registered (yet). Also returns a race that may have been removed from containing structures which may
     * lead this tracker to no longer update their {@link TrackedRace} with new data.
     */
    RaceDefinition getRace();
    
    RegattaAndRaceIdentifier getRaceIdentifier();

    RaceHandle getRaceHandle();

    DynamicTrackedRegatta getTrackedRegatta();
    
    WindStore getWindStore();
    
    /**
     * returns a unique key for this tracker which can, e.g., be used as a key in a {@link Map}
     */
    Object getID();
    
    /**
     * Listener interface for race tracker related events
     */
    @FunctionalInterface
    interface Listener {
        /**
         * Tracker has stopped event, see {@link RaceTracker#stop(boolean)} method
         * 
         * @param preemptive
         *            whether to stop ongoing loading jobs
         * @param willBeRemoved
         *            if {@code true}, the race is about to be removed; hence, no need in resuming any caches or other
         *            (re-)calculation jobs
         */
        void onTrackerWillStop(boolean preemptive, boolean willBeRemoved);
    }
    
    @FunctionalInterface
    interface RaceCreationListener {
        /**
         * Tracker has received its {@link RaceDefinition}, so that now {@link RaceTracker#getRace} no longer returns
         * {@code null} but a valid {@link RaceDefinition}
         * 
         * @param tracker
         *            this tracker is passed to the listener which can then obtain the {@link RaceTracker#getRace()
         *            race} and the {@link RaceTracker#getConnectivityParams() connectivity parameters}, etc. If the
         *            {@link RaceTracker} already has created its race, this method is called immediately upon
         *            {@link RaceTracker#add(Listener) adding} this listener so that the listener will always receive
         *            the call for a valid race if the race is created at any point in time, regardless the point in
         *            time of the listener registration. This helps avoid race conditions.
         */
        void onRaceCreated(RaceTracker tracker);
    }

    /**
     * Register a new RaceTracker.Listener for this race tracker.
     * 
     * @return true if listener has been added
     */
    boolean add(RaceTracker.Listener newListener);

    /**
     * Remove listener from race tracker
     * 
     * @param listener
     * @return the listener registration for listener removal
     */
    void remove(RaceTracker.Listener listener);
    
    /**
     * Adds a listener that will be {@link RaceCreationListener#onRaceCreated(RaceTracker) notified} when the
     * {@link #getRace()} method starts returning a valid, non-{@code null} race. The listener will be notified
     * immediately if {@link #getRace()} already yields a valid {@link RaceDefinition} when calling this method so
     * that the listener will be notified at least once. The listener will automatically be removed after it has been
     * notified. 
     */
    void add(RaceTracker.RaceCreationListener listener);
    
    void remove(RaceTracker.RaceCreationListener listener);

    /**
     * The connectivity parameters used to create this tracker. Can be used, e.g., to add or remove those parameters to
     * the set of trackers to restore after a server restart. May return {@code null}, e.g., in case the tracker was
     * created by a test case that did not use a {@link RaceTrackingConnectivityParameters} object to describe what to
     * track.
     */
    RaceTrackingConnectivityParameters getConnectivityParams();
}
