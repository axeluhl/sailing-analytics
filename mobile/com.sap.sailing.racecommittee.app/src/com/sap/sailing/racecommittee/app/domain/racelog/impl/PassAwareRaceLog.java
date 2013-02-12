package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PassAwareRaceLog extends RaceLogImpl implements RaceLog {
	private static final String TAG = PassAwareRaceLog.class.getName();
	private static final long serialVersionUID = -1252381252528365834L;
	private static final String ReadWriteLockName =  PassAwareRaceLog.class.getName() + ".lock";
	
	public static final int InvalidPassId = -1;
	
	private int currentPassId;
	
	public PassAwareRaceLog() {
		this(InvalidPassId);
	}

	public PassAwareRaceLog(int currentPassId) {
		super(ReadWriteLockName);
		this.currentPassId = currentPassId;
	}
	
	public int getCurrentPassId() {
		return currentPassId;
	}
	
	public void setCurrentPassId(int currentPass) {
		ExLog.i(TAG, String.format("Incrementing pass id to %d", currentPass));
		this.currentPassId = currentPass;
	}
	
	@Override
	public NavigableSet<RaceLogEvent> getFixes() {
		lockForRead();
		try {
			return super.getFixes();
		} finally {
			unlockAfterRead();
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
	
	@Override
	public boolean add(RaceLogEvent event) {
		if (super.add(event)) {
			setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
			return true;
		}
		return false;
	}
}
