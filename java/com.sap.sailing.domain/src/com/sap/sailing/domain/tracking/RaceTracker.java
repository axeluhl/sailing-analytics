package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;

/**
 * Centerpiece of a tracking adapter. A tracker is responsible for receiving tracking data for one or more
 * {@link RaceDefinition races} that are {@link Regatta#getAllRaces() part of} a common {@link #getRegatta() Event}. Some
 * tracker architectures may not be able to deliver all data for the {@link RaceDefinition} when created or started.
 * Therefore, {@link #getRaces()} may return <code>null</code> if the race information hasn't been received by the
 * tracker yet. Through the {@link RaceHandle} returned by {@link #getRacesHandle()} it is also possible to perform a
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
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface RaceTracker {
    /**
     * By default, wait one minute for race data; sometimes, a tracking provider's server may be under heavy load and
     * may serve races one after another. If many races are requested concurrently, this can lead to a queue
     * of several minutes length.
     */
    static long TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS = 60000;

    /**
     * Stops tracking the races.
     * @param preemptive TODO
     */
    void stop(boolean preemptive) throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Regatta getRegatta();

    /**
     * Returns the races being tracked by this tracker. Non-blocking call that returns <code>null</code> or an empty set
     * if the {@link RaceDefinition} for a TracTrac Event hasn't been created yet, e.g., because the course definition
     * hasn't been received yet or the listener for receiving course information hasn't been registered (yet). Also
     * returns races that have been removed from containing structures which may lead this tracker to no longer update
     * their {@link TrackedRace} with new data.
     */
    Set<RaceDefinition> getRaces();
    
    Set<RegattaAndRaceIdentifier> getRaceIdentifiers();

    RaceHandle getRacesHandle();

    DynamicTrackedRegatta getTrackedRegatta();
    
    WindStore getWindStore();
    
    GPSFixStore getGPSFixStore();

    /**
     * returns a unique key for this tracker which can, e.g., be used as a key in a {@link Map}
     */
    Object getID();
    
}
