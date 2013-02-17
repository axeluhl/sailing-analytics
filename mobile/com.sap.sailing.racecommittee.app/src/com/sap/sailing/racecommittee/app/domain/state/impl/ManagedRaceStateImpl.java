package com.sap.sailing.racecommittee.app.domain.state.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.racecommittee.app.domain.state.ManagedRaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class ManagedRaceStateImpl implements ManagedRaceState, RaceLogChangedListener {
private static final String TAG = ManagedRaceStateImpl.class.getName();
	
	protected RaceLogRaceStatus status;
	protected RaceLog raceLog;
	
	private RaceLogChangedVisitor raceLogListener;
	
	public ManagedRaceStateImpl(RaceLog raceLog) {
		this.raceLogListener = new RaceLogChangedVisitor(this);
		this.raceLog = raceLog;
		this.status = RaceLogRaceStatus.UNKNOWN;
		
		this.raceLog.addListener(raceLogListener);
		
		updateStatus();
	}

	public RaceLog getRaceLog() {
		return raceLog;
	}

	public RaceLogRaceStatus getStatus() {
		return status;
	}
	
	private void setStatus(RaceLogRaceStatus status) {
		this.status = status;
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
