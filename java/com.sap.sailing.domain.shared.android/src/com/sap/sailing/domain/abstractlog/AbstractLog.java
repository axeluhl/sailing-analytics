package com.sap.sailing.domain.abstractlog;

import java.io.Serializable;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RevokeEvent;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.racelog.tracking.NotRevokableException;
import com.sap.sailing.domain.tracking.Track;

/**
 * Special kind of {@link Track} for recording {@link AbstractLogEvent}s.
 * 
 * <p>
 * Keeps track of the {@link AbstractLogEvent}'s pass and returns only the events of the current pass on
 * {@link RaceLog#getFixes()}. Use {@link RaceLog#getRawFixes()} to receive all events in a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Implementations should use the {@link AbstractLogEventComparator} for sorting its content.
 * </p>
 */
public interface AbstractLog<T extends AbstractLogEvent> extends Track<T>, WithID {
    
    public static final int DefaultPassId = 0;
    
    /**
     * Gets the current pass id.
     * 
     * @return the pass id.
     */
    int getCurrentPassId();

    /**
     * Adds a {@link AbstractLogEvent} to the {@link RaceLog}.
     * 
     * @param event
     *            {@link AbstractLogEvent} to be added.
     * @return <code>true</code> if the element was added, <code>false</code> otherwise.
     */
    boolean add(T event);

    /**
     * Add a {@link RaceLogEventVisitor} as a listener for additions.
     */
    void addListener(RaceLogEventVisitor listener);

    /**
     * Remove a listener.
     */
    void removeListener(RaceLogEventVisitor listener);
    
    /**
     * Removes all listeners
     * @return 
     */
    HashSet<RaceLogEventVisitor> removeAllListeners();
    
    /**
     * Checks if the race log is empty.
     */
    boolean isEmpty();

    Iterable<T> getRawFixesDescending();

    Iterable<T> getFixesDescending();

    void addAllListeners(HashSet<RaceLogEventVisitor> listeners);

    Iterable<RaceLogEventVisitor> getAllListeners();

    /**
     * Adds an event to this race log and returns {@link RaceLog#getEventsToDeliver(UUID)} 
     * (excluding the new <code>event</code>)
     */
    Iterable<T> add(T event, UUID clientId);
    
    /**
     * Returns a superset of all race log events that were added to this race log but not yet returned to 
     * the client with ID <code>clientId</code> by this method. In general, the list returned is not a true 
     * superset but equals exactly those events not yet delivered to the client. However, if the server 
     * was re-started since the client last called this method, and since the underlying data structures 
     * are not durably stored, the entire set of all race log events would be delivered to the client once.
     */
    Iterable<T> getEventsToDeliver(UUID clientId);
    
    /**
     * Returns all {@link #getRawFixes() raw fixes} and marks them as delivered to the client identified by <code>clientId</code>
     * so that when that ID appears in a subsequent call to {@link #add(AbstractLogEvent, UUID)}, the fixes returned by this call
     * are already considered delivered to the client identified by <code>clientId</code>.
     */
    Iterable<T> getRawFixes(UUID clientId);

    /**
     * Like {@link #add(AbstractLogEvent)}, only that no events are triggered. Use this method only when loading a race log,
     * e.g., from a replication or master data import or when loading from the database.
     * 
     * @return <code>true</code> if the event was actually added which is the case if there was no equal event contained
     *         in this race log yet
     */
    boolean load(T event);
    
    /**
     * Search for the event by its {@link AbstractLogEvent#getId() id}.
     * Caller needs to hold the read lock.
     */
    T getEventById(Serializable id);
    
    /**
     * Get a {@link NavigableSet} of unrevoked events regardless of the {@code pass}. Events are sorted by
     * their {@link TimePoint} and the oldest is returned first.
     * @return
     */
    NavigableSet<T> getUnrevokedEvents();
    
    /**
     * Get a {@link NavigableSet} of unrevoked events regardless of the {@code pass}.
     * @return
     */
    NavigableSet<T> getUnrevokedEventsDescending();
    
    /**
     * Inserts a {@link RevokeEvent} for {@code toRevoke}, if latter is revokable, exists in the racelog
     * and has not yet been revoked.
     * 
     * @param author The author for the {@code RevokeEvent}.
     */
    RevokeEvent revokeEvent(RaceLogEventAuthor author, T toRevoke, String reason) throws NotRevokableException;
    RevokeEvent revokeEvent(RaceLogEventAuthor author, T toRevoke) throws NotRevokableException;

    /**
     * Merges all events from the <code>other</code> race log into this.
     */
    void merge(AbstractLog<T> other);
}
