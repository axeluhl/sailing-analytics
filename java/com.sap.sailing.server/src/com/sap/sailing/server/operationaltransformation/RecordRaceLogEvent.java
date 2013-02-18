package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEvent extends AbstractRacingEventServiceOperation<Void> {

	private static final long serialVersionUID = -2532392938428607006L;
	private final RaceLogIdentifier identifier;
	private final RaceLogEvent event;

	public RecordRaceLogEvent(RaceLogIdentifier identifier, RaceLogEvent event) {
		super();
		this.identifier = identifier;
		this.event = event;
	}

	@Override
	public Void internalApplyTo(RacingEventService toState) throws Exception {
		toState.recordRaceLogEvent(identifier, event);
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
