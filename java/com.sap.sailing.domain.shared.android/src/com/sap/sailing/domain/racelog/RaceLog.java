package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;
import com.sap.sailing.domain.tracking.Track;

/**
 * Special kind of {@link Track} for recording {@link RaceLogEvent}s.
 * 
 * <p>
 * Keeps track of the {@link RaceLogEvent}'s pass and returns only the events of the current pass on
 * {@link RaceLog#getFixes()}. Use {@link RaceLog#getRawFixes()} to receive all events in a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Implementations should use the {@link RaceLogEventComparator} for sorting its content.
 * </p>
 */
public interface RaceLog extends Track<RaceLogEvent>, WithID {
    /**
     * Gets the current pass id.
     * 
     * @return the pass id.
     */
    int getCurrentPassId();

    /**
     * Adds a {@link RaceLogEvent} to the {@link RaceLog}.
     * 
     * @param event
     *            {@link RaceLogEvent} to be added.
     * @return <code>true</code> if the element was added, <code>false</code> otherwise.
     */
    boolean add(RaceLogEvent event);

    /**
     * Add a {@link RaceLogEventVisitor} as a listener for additions.
     */
    void addListener(RaceLogEventVisitor listener);

    /**
     * Remove a listener.
     */
    void removeListener(RaceLogEventVisitor listener);
    
    /**
     * Checks if the race log is empty.
     */
    boolean isEmpty();

    Iterable<RaceLogEvent> getRawFixesDescending();

    Iterable<RaceLogEvent> getFixesDescending();
}
