package com.sap.sailing.domain.tracking;

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
     * Looks for tracking information about <code>event</code>. If no such object exists yet, a new one
     * is created.
     */
    DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta regatta);

    /**
     * Looks for the tracking information for <code>event</code>. If not found, <code>null</code> is returned
     * immediately. See also {@link #getOrCreateTrackedRegatta(com.sap.sailing.domain.base.Regatta)}.
     */
    DynamicTrackedRegatta getTrackedRegatta(Regatta regatta);
    
    void removeTrackedRegatta(Regatta regatta);

}
