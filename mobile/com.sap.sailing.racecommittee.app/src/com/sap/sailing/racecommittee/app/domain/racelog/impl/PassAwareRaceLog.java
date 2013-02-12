package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class PassAwareRaceLog extends TrackImpl<RaceLogEvent> implements RaceLog {
	private static final String TAG = PassAwareRaceLog.class.getName();
	private static final long serialVersionUID = -1252381252528365834L;
	private static final String ReadWriteLockName =  PassAwareRaceLog.class.getName() + ".lock";
	
	public static final int InvalidPassId = -1;
	
	private int currentPassId;
	
	public PassAwareRaceLog() {
		this(InvalidPassId);
	}

	public PassAwareRaceLog(int currentPassId) {
		super(new ArrayListNavigableSet<Timed>(RaceLogEventComparator.INSTANCE), ReadWriteLockName);
		this.currentPassId = currentPassId;
	}
	
	protected PassAwareRaceLog(NavigableSet<Timed> fixes) {
		super(fixes, ReadWriteLockName);
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
	
	public boolean add(RaceLogEvent event) {
		lockForWrite();
		try {
			if (getInternalRawFixes().add(event)) {
				setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
				return true;
			}
			return false;
		} finally {
			unlockAfterWrite();
		}
	}

	public void addListener(RaceLogListener newListener) {
		/// TODO: do we need this?
	}

}
