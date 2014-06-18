package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;

import com.sap.sailing.domain.base.Event;

/**
 * Manages a set of events and can resolve one by the event's ID
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface EventResolver {
    /**
     * Returns the event with given id. When no event is found, <b>null</b> is returned.
     * 
     * @param id
     *                  The id of the event.
     * @return The event with given id.
     */
    Event getEvent(Serializable id);
}
