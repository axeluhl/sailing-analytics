package com.sap.sailing.domain.racecommittee.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeListener;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class RaceCommitteeEventTrackImpl extends TrackImpl<RaceCommitteeEvent> implements RaceCommitteeEventTrack {

	private static final long serialVersionUID = -176745401321893502L;
	
	private transient Set<RaceCommitteeListener> listeners;
	
	private final static Logger logger = Logger.getLogger(RaceCommitteeEventTrackImpl.class.getName());

	public RaceCommitteeEventTrackImpl(String nameForReadWriteLock) {
		super(new ArrayListNavigableSet<Timed>(RaceCommitteeEventComparator.INSTANCE), nameForReadWriteLock);
		listeners = new HashSet<RaceCommitteeListener>();
	}

	@Override
	public void add(RaceCommitteeEvent event) {
		System.out.println("RaceCommitteeEventTrackImpl.add() called for RC Event" + event + " with timestamp " + event.getTimePoint().toString() + " on " + this.toString());
		lockForWrite();
		System.out.println("RaceCommitteeEventTrackImpl.lockForWrite() called for RC Event" + event + " with timestamp " + event.getTimePoint().toString());
        try {
            getInternalRawFixes().add(event);
            System.out.println("RaceCommitteeEventTrackImpl.getInternalRawFixes().add() called for RC Event" + event + " with timestamp " + event.getTimePoint().toString());
        } finally {
            unlockAfterWrite();
            System.out.println("RaceCommitteeEventTrackImpl.unlockAfterWrite() called for RC Event" + event + " with timestamp " + event.getTimePoint().toString());
        }
        notifyListenersAboutReceive(event);
	}
	
	private void notifyListenersAboutReceive(RaceCommitteeEvent event) {
		if (event instanceof RaceCommitteeFlagEvent) {
			notifiyListenersAboutReceivedFlagEvent((RaceCommitteeFlagEvent) event);
		} else if (event instanceof RaceCommitteeStartTimeEvent) {
			notifiyListenersAboutReceivedStartTimeEvent((RaceCommitteeStartTimeEvent) event);
		}
    }
	
	private void notifiyListenersAboutReceivedStartTimeEvent(RaceCommitteeStartTimeEvent event) {
		synchronized (listeners) {
            for (RaceCommitteeListener listener : listeners) {
                try {
                    listener.startTimeEventReceived(event);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceCommitteeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(RaceCommitteeEventTrackImpl.class.getName(), "notifiyListenersAboutReceivedStartTimeEvent(RaceCommitteeStartTimeEvent)", t);
                }
            }
        }
	}

	private void notifiyListenersAboutReceivedFlagEvent(RaceCommitteeFlagEvent event) {
		synchronized (listeners) {
            for (RaceCommitteeListener listener : listeners) {
                try {
                    listener.flagEventReceived(event);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceCommitteeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(RaceCommitteeEventTrackImpl.class.getName(), "notifiyListenersAboutReceivedFlagEvent(RaceCommitteeFlagEvent)", t);
                }
            }
        }
	}

	@Override
    public void addListener(RaceCommitteeListener newListener) {
        synchronized (listeners) {
            listeners.add(newListener);
        }
    }

}
