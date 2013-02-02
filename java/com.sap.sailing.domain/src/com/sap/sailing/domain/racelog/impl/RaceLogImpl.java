package com.sap.sailing.domain.racelog.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class RaceLogImpl extends TrackImpl<RaceLogEvent> implements RaceLog {

	private static final long serialVersionUID = -176745401321893502L;
	
	private transient Set<RaceLogListener> listeners;
	
	private final static Logger logger = Logger.getLogger(RaceLogImpl.class.getName());

	public RaceLogImpl(String nameForReadWriteLock) {
		super(new ArrayListNavigableSet<Timed>(RaceLogEventComparator.INSTANCE), nameForReadWriteLock);
		listeners = new HashSet<RaceLogListener>();
	}

	@Override
	public void add(RaceLogEvent event) {
		lockForWrite();
        try {
            getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        notifyListenersAboutReceive(event);
	}
	
	private void notifyListenersAboutReceive(RaceLogEvent event) {
		if (event instanceof RaceLogFlagEvent) {
			notifiyListenersAboutReceivedFlagEvent((RaceLogFlagEvent) event);
		} else if (event instanceof RaceLogStartTimeEvent) {
			notifiyListenersAboutReceivedStartTimeEvent((RaceLogStartTimeEvent) event);
		}
    }
	
	private void notifiyListenersAboutReceivedStartTimeEvent(RaceLogStartTimeEvent event) {
		synchronized (listeners) {
            for (RaceLogListener listener : listeners) {
                try {
                    listener.startTimeEventReceived(event);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceLogListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(RaceLogImpl.class.getName(), "notifiyListenersAboutReceivedStartTimeEvent(RaceLogStartTimeEvent)", t);
                }
            }
        }
	}

	private void notifiyListenersAboutReceivedFlagEvent(RaceLogFlagEvent event) {
		synchronized (listeners) {
            for (RaceLogListener listener : listeners) {
                try {
                    listener.flagEventReceived(event);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceLogListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(RaceLogImpl.class.getName(), "notifiyListenersAboutReceivedFlagEvent(RaceLogFlagEvent)", t);
                }
            }
        }
	}

	@Override
    public void addListener(RaceLogListener newListener) {
        synchronized (listeners) {
            listeners.add(newListener);
        }
    }

}
