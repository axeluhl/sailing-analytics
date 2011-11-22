package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Event;

/**
 * {@link TrackedEvent}s or {@link DynamicTrackedEvent}s belong to {@link Event} objects. However, an {@link Event}
 * doesn't and shouldn't know how it's being tracked. A <em>registry</em> decouples them. Test cases may use
 * different, simplified registries.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TrackedEventRegistry {
    /**
     * Looks for tracking information about <code>event</code>. If no such object exists yet, a new one
     * is created.
     */
    DynamicTrackedEvent getOrCreateTrackedEvent(Event event);

    /**
     * Looks for the tracking information for <code>event</code>. If not found, <code>null</code> is returned
     * immediately. See also {@link #getOrCreateTrackedEvent(com.sap.sailing.domain.base.Event)}.
     */
    DynamicTrackedEvent getTrackedEvent(com.sap.sailing.domain.base.Event event);
    
    void remove(Event event);

}
