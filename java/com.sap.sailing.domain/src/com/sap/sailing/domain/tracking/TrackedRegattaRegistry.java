package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;

/**
 * {@link TrackedRegatta}s or {@link DynamicTrackedRegatta}s belong to {@link Regatta} objects. However, an {@link Regatta}
 * doesn't and shouldn't know how it's being tracked. A <em>registry</em> decouples them. Test cases may use
 * different, simplified registries.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TrackedRegattaRegistry {
    /**
     * Looks for tracking information about <code>regatta</code>. If no such object exists yet, a new one
     * is created.
     */
    DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta regatta);

    /**
     * Looks for the tracking information for <code>regatta</code>. If not found, <code>null</code> is returned
     * immediately. See also {@link #getOrCreateTrackedRegatta(com.sap.sailing.domain.base.Regatta)}.
     */
    DynamicTrackedRegatta getTrackedRegatta(Regatta regatta);
    
    /**
     * Removes <code>race</code> and any corresponding {@link #getTrackedRace(Regatta, RaceDefinition) tracked race}
     * from this service. If it was the last {@link RaceDefinition} in its {@link Regatta} and the regatta
     * {@link Regatta#isPersistent() is not stored persistently}, the <code>regatta</code> is removed as well and will no
     * longer be returned by {@link #getAllRegattas()}. The wind tracking is stopped for <code>race</code>.
     * <p>
     * 
     * Any {@link RaceTracker} for which <code>race</race> is the last race tracked that is still reachable
     * from {@link #getAllRegattas()} will be {@link RaceTracker#stop(boolean) stopped}.
     * 
     * The <code>race</code> will be also removed from all leaderboards containing a column that has <code>race</code>'s
     * {@link #getTrackedRace(Regatta, RaceDefinition) corresponding} {@link TrackedRace} as its
     * {@link RaceColumn#getTrackedRace(Fleet)}.
     * 
     * @param regatta
     *            the regatta from which to remove the race
     * @param race
     *            the race to remove
     */
    void removeRace(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException,InterruptedException;

    void removeTrackedRegatta(Regatta regatta);

    /**
     * A race needs to be associated with a {@link Regatta} so that {@link Regatta#getAllRaces()} returns it. Which
     * regatta to associate the race with should usually be decided by the user. Once explicitly decided and stored in
     * this registry by calling {@link #setRegattaForRace(Regatta, RaceDefinition)}, this information is preserved by
     * this registry for future use so that when the race's {@link RaceDefinition#getId() ID} is recognized, the same
     * regatta will be returned.
     * <p>
     * 
     * If no such explicit assignment has been performed, <code>null</code> is returned.
     * 
     * @param raceID the ID as obtained from {@link RaceDefinition#getId()}
     */
    Regatta getRememberedRegattaForRace(Serializable raceID);

    boolean isRaceBeingTracked(Regatta regattaContext, RaceDefinition r);

    /**
     * Stops all {@link RaceTracker}s currently tracking <code>race</code>. Note that if the same tracker also may have
     * been tracking other races. Other races of the same event that are currently tracked will continue to be tracked.
     * If wind tracking for the race is currently running, it will be stopped (see also
     * {@link #stopTrackingWind(Regatta, RaceDefinition)}). The <code>race</code> (and the other races tracked by the
     * same tracker) as well as the corresponding {@link TrackedRace}s will continue to exist, e.g., when asking
     * {@link #getTrackedRace(Regatta, RaceDefinition)}.
     */
    void stopTracking(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * Stops a defined {@link RaceTracker} that probably did not already get to know which race is about to be tracked.
     * This can be used to stop a RaceTracker that is in an error state before resolution of the race(s) to track is
     * successful. In this case it is not practicable to stop tracking by a {@link Regatta}/{@link RaceDefinition} pair
     * due to the fact that this would remove all RaceTrackers that currently have no {@link RaceDefinition} resolved
     * yet.
     */
    void stopTracker(Regatta regatta, RaceTracker tracker) throws MalformedURLException, IOException, InterruptedException;

}
