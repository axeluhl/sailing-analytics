package com.sap.sailing.racecommittee.app.domain.state.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RaceStateImpl implements RaceState, RaceLogChangedListener {
private static final String TAG = RaceStateImpl.class.getName();
	
	protected RaceLogRaceStatus status;
	protected RaceLog raceLog;
	protected Set<RaceStateChangedListener> changedListeners;
	
	private RaceLogChangedVisitor raceLogListener;
	
	public RaceStateImpl(RaceLog raceLog) {
		this.raceLog = raceLog;
		this.status = RaceLogRaceStatus.UNKNOWN;
		this.changedListeners = new HashSet<RaceStateChangedListener>();
		
		this.raceLogListener = new RaceLogChangedVisitor(this);
		this.raceLog.addListener(raceLogListener);
		
		updateStatus();
	}

	public RaceLog getRaceLog() {
		return raceLog;
	}

	public RaceLogRaceStatus getStatus() {
		return status;
	}
	
	private void setStatus(RaceLogRaceStatus newStatus) {
		RaceLogRaceStatus oldStatus = this.status;
		this.status = newStatus;
		if (oldStatus != newStatus) {
			notifyListeners();
		}
	}

	private void notifyListeners() {
		for (RaceStateChangedListener listener : changedListeners) {
			listener.onRaceStateChanged(this);
		}
	}

	public void registerListener(RaceStateChangedListener listener) {
		changedListeners.add(listener);
	}

	public void unregisterListener(RaceStateChangedListener listener) {
		changedListeners.remove(listener);
	}

	public RaceLogRaceStatus updateStatus() {
		ExLog.i(TAG, String.format("Updating status..."));
		
		RaceLogRaceStatus newStatus = RaceLogRaceStatus.UNSCHEDULED;
		for (RaceLogEvent event : raceLog.getFixes()) {
			ExLog.i(TAG, String.format("Deciding on event of type %s.", event.getClass().getSimpleName()));
			if (event instanceof RaceLogRaceStatusEvent) {
				RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
				ExLog.i(TAG, String.format("Decision to %s.", statusEvent.getNextStatus()));
				newStatus = statusEvent.getNextStatus();
			}
		}
		
		ExLog.i(TAG, String.format("Status will be set to %s.", newStatus));
		setStatus(newStatus);
		
		return getStatus();
	}

	public void eventAdded(RaceLogEvent event) {
		updateStatus();
	}

}
