package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEvent extends AbstractRacingEventServiceOperation<RaceLogEvent> {

	private static final long serialVersionUID = 1539017717057956058L;
	private final String namedName;
	private final String raceColumnName;
	private final String fleetName;
	
	public RecordRaceLogEvent(String namedName, String raceColumnName, String fleetName) {
		this.namedName = namedName;
		this.raceColumnName = raceColumnName;
		this.fleetName = fleetName;
	}

	@Override
	public RaceLogEvent internalApplyTo(RacingEventService toState) throws Exception {
		return null;
	}

	@Override
	public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
		return null;
	}

	@Override
	public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
		return null;
	}

}
