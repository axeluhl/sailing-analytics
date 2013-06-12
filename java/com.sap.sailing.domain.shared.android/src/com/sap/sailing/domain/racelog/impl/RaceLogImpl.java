package com.sap.sailing.domain.racelog.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

/**
 * "Fix" validity is decided based on the {@link #getCurrentPassId() current pass}. The validity is not cached.
 */
public class RaceLogImpl extends TrackImpl<RaceLogEvent> implements RaceLog {
    private static final long serialVersionUID = -176745401321893502L;
    private static final String DefaultLockName = RaceLogImpl.class.getName() + ".lock";
    private final static Logger logger = Logger.getLogger(RaceLogImpl.class.getName());
    
    private final Serializable id;
    private transient Set<RaceLogEventVisitor> listeners;
    private int currentPassId;
    
    /**
     * Initializes a new {@link RaceLogImpl} with the default lock name.
     */
    public RaceLogImpl(Serializable identifier) {
        this(DefaultLockName, identifier);
    }

    /**
     * Initializes a new {@link RaceLogImpl}.
     * @param nameForReadWriteLock name of lock.
     */
    public RaceLogImpl(String nameForReadWriteLock, Serializable identifier) {
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
     * Sets a new active pass id.
     * Ignored if new and current are equal.
     * @param newPassId to be set.
     */
    public void setCurrentPassId(int newPassId) {
        if (newPassId != this.currentPassId) {
            logger.finer(String.format("Changing pass id to %d", newPassId));
            this.currentPassId = newPassId;
        }
    }

    @Override
    public boolean add(RaceLogEvent event) {
        boolean isAdded = false;
        lockForWrite();
        try {
            isAdded = getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        if (isAdded) {
            logger.finer(String.format("%s (%s) was added to log.", event, event.getClass().getName()));
            setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
            notifyListenersAboutReceive(event);
        } else {
            logger.warning(String.format("%s (%s) was not added to log. Ignoring", event, event.getClass().getName()));
        }
        return isAdded;
    }

    protected void notifyListenersAboutReceive(RaceLogEvent event) {
        synchronized (listeners) {
            for (RaceLogEventVisitor listener : listeners) {
                try {
                    event.accept(listener);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceLogEventVisitor " + listener + " threw exception " + t.getMessage());
                    logger.throwing(RaceLogImpl.class.getName(), "notifyListenersAboutReceive(RaceLogEvent)", t);
                }
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
    protected NavigableSet<RaceLogEvent> getInternalFixes() {
        return new PartialNavigableSetView<RaceLogEvent>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(RaceLogEvent e) {
                return e.getPassId() == getCurrentPassId();
            }
        };
    }
    
    /**
     * When deserializing, needs to initialize empty set of listeners.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<RaceLogEventVisitor>();
    }

    @Override
    public Iterable<RaceLogEvent> getRawFixesDescending() {
        return getRawFixes().descendingSet();
    }
    
    @Override
    public Iterable<RaceLogEvent> getFixesDescending() {
        return getFixes().descendingSet();
    }

}
