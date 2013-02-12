package com.sap.sailing.racecommittee.app.domain.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.tracking.impl.TrackImpl;

public class RaceLogImpl extends TrackImpl<RaceLogEvent> implements RaceLog {
	private static final long serialVersionUID = -1252381252528365834L;
	private static final String ReadWriteLockName =  RaceLogImpl.class.getName() + ".lock";
			
	protected RaceLogImpl(NavigableSet<Timed> fixes) {
		super(fixes, ReadWriteLockName);
	}

	public RaceLogImpl() {
		super(ReadWriteLockName);
	}

	public void add(RaceLogEvent event) {
		getInternalRawFixes().add(event);
	}

	public void addListener(RaceLogListener newListener) {
		/// TODO: do we need this?
	}

}
