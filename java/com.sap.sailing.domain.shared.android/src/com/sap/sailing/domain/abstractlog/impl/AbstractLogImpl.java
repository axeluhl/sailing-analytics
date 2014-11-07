package com.sap.sailing.domain.abstractlog.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.Revokable;
import com.sap.sailing.domain.abstractlog.race.RevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventComparator;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.tracking.NotRevokableException;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

/**
 * {@link Track} implementation for {@link RaceLogEvent}s.
 * 
 * <p>
 * "Fix" validity is decided based on the {@link #getCurrentPassId() current pass}. The validity is not cached.
 * </p>
 * 
 * <p>
 * {@link TrackImpl#getDummyFix(com.sap.sailing.domain.common.TimePoint)} is not overridden, see
 * {@link RaceLogEventComparator} for sorting when interface methods like
 * {@link Track#getFirstFixAfter(com.sap.sailing.domain.common.TimePoint)} are used.
 * </p>
 * 
 */
public class AbstractLogImpl<T extends AbstractLogEvent> extends TrackImpl<T> implements AbstractLog<T> {
    private static final long serialVersionUID = -176745401321893502L;
    private static final String DefaultLockName = AbstractLogImpl.class.getName() + ".lock";
    private final static Logger logger = Logger.getLogger(AbstractLogImpl.class.getName());
    private Set<Serializable> revokedEventIds = new HashSet<Serializable>();

    /**
     * Clients can use the {@link #add(RaceLogEvent, UUID)} method
     */
    private transient Map<UUID, Set<T>> eventsDeliveredToClient = new HashMap<UUID, Set<T>>();
    
    private Map<Serializable, T> eventsById = new HashMap<Serializable, T>();

    private final Serializable id;
    private transient Set<RaceLogEventVisitor> listeners;
    private int currentPassId;

    /**
     * Initializes a new {@link RaceLogImpl} with the default lock name.
     */
    public AbstractLogImpl(Serializable identifier) {
        this(DefaultLockName, identifier);
    }

    /**
     * Initializes a new {@link RaceLogImpl}.
     * 
     * @param nameForReadWriteLock
     *            name of lock.
     */
    public AbstractLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(new ArrayListNavigableSet<Timed>(RaceLogEventComparator.INSTANCE), nameForReadWriteLock);

        this.listeners = new HashSet<RaceLogEventVisitor>();
        this.currentPassId = DefaultPassId;
        this.id = identifier;
    }

    @Override
    public Serializable getId() {
        return this.id;
    }

    @Override
    public int getCurrentPassId() {
        return currentPassId;
    }

    /**
     * Sets a new active pass id. Ignored if new and current are equal.
     * 
     * @param newPassId
     *            to be set.
     */
    public void setCurrentPassId(int newPassId) {
        if (newPassId != this.currentPassId) {
            logger.finer(String.format("Changing pass id to %d", newPassId));
            this.currentPassId = newPassId;
        }
    }
    
    @Override
    public boolean add(T event) {
        boolean isAdded = false;
        lockForWrite();
        try {
            isAdded = getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        if (isAdded) {
            logger.finer(String.format("%s (%s) was added to log %s.", event, event.getClass().getName(), getId()));
            // FIXME with out-of-order delivery would destroy currentPassId; need to check at least the createdAt time
            // point
            setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
            revokeIfNecessary(event);
            eventsById.put(event.getId(), event);
            notifyListenersAboutReceive(event);
        } else {
            logger.fine(String.format("%s (%s) was not added to race log %s because it already existed there.", event, event.getClass().getName(), getId()));
        }
        return isAdded;
    }

    @Override
    public boolean load(T event) {
        boolean isAdded = false;
        lockForWrite();
        try {
            isAdded = getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        if (isAdded) {
            logger.finer(String.format("%s (%s) was loaded into log.", event, event.getClass().getName()));
            setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
            revokeIfNecessary(event);
            eventsById.put(event.getId(), event);
        } else {
            logger.finer(String
                    .format("%s (%s) was not loaded into log because it already existed there.", event, event.getClass().getName()));
        }
        return isAdded;
    }
    
    private void revokeIfNecessary(T newEvent) {
        if (newEvent instanceof RevokeEvent) {
            RevokeEvent revokeEvent = (RevokeEvent) newEvent;
            try {
                checkIfSuccessfullyRevokes(revokeEvent);
                lockForWrite();
                revokedEventIds.add(revokeEvent.getRevokedEventId());
                unlockAfterWrite();
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }
    }
    
    private void checkIfSuccessfullyRevokes(RevokeEvent revokeEvent) throws NotRevokableException {
        lockForRead();
        T revokedEvent = getEventById(revokeEvent.getRevokedEventId());
        unlockAfterRead();

        if (revokedEvent == null) {
            // it can happen that the event that has been revoked is not yet loaded - as we assume
            // that race log events never get removed we can safely continue and assume that
            // the event will be loaded later
            logger.warning("RevokeEvent for "+revokeEvent.getShortInfo()+" added, that refers to non-existent event to be revoked. Could also happen that the revoke event is before the event to be revoked.");
        } else {
            if (! (revokedEvent instanceof Revokable)) {
                throw new NotRevokableException("RevokeEvent trying to revoke non-revokable event");
            }

            // make sure to compare only author priorities - assuming that revoke events are
            // independent of passes and times
            if (revokeEvent.getAuthor().getPriority() > revokedEvent.getAuthor().getPriority()) {
                throw new NotRevokableException("RevokeEvent does not have sufficient priority");
            }
        }
    }

    @Override
    public Iterable<T> add(T event, UUID clientId) {
        add(event);
        return getEventsToDeliver(clientId, event);
    }
    
    @Override
    public Iterable<T> getEventsToDeliver(UUID clientId) {
        return getEventsToDeliver(clientId, null);
    }

    protected Iterable<T> getEventsToDeliver(UUID clientId, T suppressedEvent) {
        final LinkedHashSet<T> stillToDeliverToClient;
        lockForRead();
        try {
            stillToDeliverToClient = new LinkedHashSet<T>(getInternalRawFixes());
        } finally {
            unlockAfterRead();
        }
        stillToDeliverToClient.remove(suppressedEvent);
        Set<T> deliveredToClient = eventsDeliveredToClient.get(clientId);
        if (deliveredToClient != null) {
            stillToDeliverToClient.removeAll(deliveredToClient);
        } else {
            deliveredToClient = new HashSet<T>();
            eventsDeliveredToClient.put(clientId, deliveredToClient);
        }
        deliveredToClient.addAll(stillToDeliverToClient);
        deliveredToClient.add(suppressedEvent);
        return stillToDeliverToClient;
    }

    protected void notifyListenersAboutReceive(T event) {
        Set<RaceLogEventVisitor> workingListeners = new HashSet<RaceLogEventVisitor>();
        synchronized (listeners) {
            workingListeners.addAll(listeners);
        }
        for (RaceLogEventVisitor listener : workingListeners) {
            try {
                event.accept(listener);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceLogEventVisitor " + listener + " threw exception " + t.getMessage());
                logger.throwing(RaceLogImpl.class.getName(), "notifyListenersAboutReceive(RaceLogEvent)", t);
                t.printStackTrace();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getFirstRawFix() == null;
    }

    @Override
    public void addListener(RaceLogEventVisitor listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(RaceLogEventVisitor listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    protected NavigableSet<T> getInternalFixes() {
        return new PartialNavigableSetView<T>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(T e) {
                return e.getPassId() == getCurrentPassId();
            }
        };
    }

    /**
     * When deserializing, needs to initialize empty set of listeners. Furthermore, as a migration effort, when the
     * {@link #eventsById} field was introduced, old clients get <code>null</code> as its value when deserializing which
     * leads to NPEs later on. However, since the map is redundant to the contents of the <code>fixes</code> collection,
     * it can be reconstructed here.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<RaceLogEventVisitor>();
        eventsDeliveredToClient = new HashMap<UUID, Set<T>>();
        if (eventsById == null) {
            eventsById = new HashMap<Serializable, T>();
            lockForRead();
            try {
                for (T event : getRawFixes()) {
                    eventsById.put(event.getId(), event);
                }
            } finally {
                unlockAfterRead();
            }
        }
        if (revokedEventIds == null) {
            revokedEventIds = new HashSet<Serializable>();
            lockForRead();
            try {
                for (T event : getRawFixes()) {
                    if (event instanceof RevokeEvent) {
                        revokedEventIds.add(((RevokeEvent)event).getRevokedEventId());
                    }
                }
            } finally {
                unlockAfterRead();
            }
        }
    }

    @Override
    public Iterable<T> getRawFixesDescending() {
        return getRawFixes().descendingSet();
    }

    @Override
    public Iterable<T> getFixesDescending() {
        return getFixes().descendingSet();
    }

    @Override
    public HashSet<RaceLogEventVisitor> removeAllListeners() {
        synchronized (listeners) {
            HashSet<RaceLogEventVisitor> clonedListeners = new HashSet<RaceLogEventVisitor>(listeners);
            listeners = new HashSet<RaceLogEventVisitor>();
            return clonedListeners;
        }
    }

    @Override
    public void addAllListeners(HashSet<RaceLogEventVisitor> listeners) {
        synchronized (listeners) {
            this.listeners.addAll(listeners);
        }
    }

    @Override
    public Iterable<RaceLogEventVisitor> getAllListeners() {
        return this.listeners;
    }

    @Override
    public Iterable<T> getRawFixes(UUID clientId) {
        assertReadLock();
        NavigableSet<T> result = getRawFixes();
        Set<T> edtc = eventsDeliveredToClient.get(clientId);
        if (edtc == null) {
            edtc = new HashSet<T>();
            eventsDeliveredToClient.put(clientId, edtc);
        }
        edtc.addAll(result);
        return result;
    }

    @Override
    public T getEventById(Serializable id) {
        assertReadLock();
        return eventsById.get(id);
    }
    
    @Override
    public NavigableSet<T> getUnrevokedEvents() {
        return new PartialNavigableSetView<T>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(T e) {
            	return ! (e instanceof RevokeEvent) && ! revokedEventIds.contains(e.getId());
            }
        };
    }
    
    @Override
    public NavigableSet<T> getUnrevokedEventsDescending() {
        return new PartialNavigableSetView<T>(super.getInternalFixes().descendingSet()) {
            @Override
            protected boolean isValid(T e) {
            	return ! (e instanceof RevokeEvent) && ! revokedEventIds.contains(e.getId());
            }
        };
    }

    @Override
    public void merge(AbstractLog<T> other) {
        lockForWrite();
        other.lockForRead();
        try {
            RaceLogEventComparator comparator = RaceLogEventComparator.INSTANCE;
            Iterator<T> thisIter = getRawFixes().iterator();
            Iterator<T> otherIter = other.getRawFixes().iterator();
            T thisEvent = null;
            T otherEvent = null;
            while (otherIter.hasNext() || otherEvent != null) {
                if (thisEvent == null && thisIter.hasNext()) {
                    thisEvent = thisIter.next();
                }
                if (otherEvent == null) {
                    otherEvent = otherIter.next();
                }
                if (thisEvent == null) {
                    // All events of this race log have been consumed; simply keep adding the events
                    // from the other race log to this race log.
                    // otherEvent has to be non-null because if thisIter didn't have a next, otherIter must have had a next
                    add(otherEvent);
                    otherEvent = null; // "consumed" otherEvent; try to grab next if a next element exists in otherIter
                } else {
                    final int comparison = comparator.compare(thisEvent, otherEvent);
                    if (comparison < 0) {
                        thisEvent = null; // skip the "lesser" race log event on this race log
                    } else if (comparison == 0) {
                        // the race log event from the other log is already contained in this log; skip both
                        thisEvent = null;
                        otherEvent = null;
                    } else {
                        // comparison > 0; we skipped on this race log until we found a "greater" event on this race log; insert otherEvent
                        add(otherEvent);
                        otherEvent = null; // "consumed"
                    }
                }
            }
        } finally {
            other.unlockAfterRead();
            unlockAfterWrite();
        }
    }
    
    @Override
    public RevokeEvent revokeEvent(RaceLogEventAuthor author, T toRevoke) throws NotRevokableException {
        return revokeEvent(author, toRevoke, null);
    }
    
    @Override
    public RevokeEvent revokeEvent(RaceLogEventAuthor author, T toRevoke, String reason) throws NotRevokableException {
        if (toRevoke == null) {
            throw new NotRevokableException("Received null as event to revoke");
        }
        RevokeEvent revokeEvent = RaceLogEventFactory.INSTANCE.createRevokeEvent(
                MillisecondsTimePoint.now(), author, getCurrentPassId(), toRevoke.getId(),
                toRevoke.getClass().getSimpleName(), toRevoke.getShortInfo(), reason);
        checkIfSuccessfullyRevokes(revokeEvent);
        add((T) revokeEvent);
        return revokeEvent;
    }
}
