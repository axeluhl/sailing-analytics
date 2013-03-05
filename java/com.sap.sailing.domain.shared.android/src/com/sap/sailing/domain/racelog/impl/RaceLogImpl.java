package com.sap.sailing.domain.racelog.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class RaceLogImpl extends TrackImpl<RaceLogEvent> implements RaceLog {

    private static final long serialVersionUID = -176745401321893502L;

    private transient Set<RaceLogEventVisitor> listeners;

    private final static Logger logger = Logger.getLogger(RaceLogImpl.class.getName());

    public RaceLogImpl(String nameForReadWriteLock) {
        super(new ArrayListNavigableSet<Timed>(RaceLogEventComparator.INSTANCE), nameForReadWriteLock);
        listeners = new HashSet<RaceLogEventVisitor>();
    }
    
    /**
     * When deserializing, needs to initialize empty set of listeners.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<RaceLogEventVisitor>();
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
            notifyListenersAboutReceive(event);
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

}
