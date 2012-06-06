package com.sap.sailing.domain.tracking;

import java.io.Serializable;

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
}
