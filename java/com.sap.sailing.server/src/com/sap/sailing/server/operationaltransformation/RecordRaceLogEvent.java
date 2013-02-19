package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEvent extends AbstractRacingEventServiceOperation<RaceLogEvent> {

	private static final long serialVersionUID = 1539017717057956058L;
	private final Named named;
	private final RaceColumn raceColumn;
	private final Fleet fleet;
	
	public RecordRaceLogEvent(Named named, RaceColumn raceColumn, Fleet fleet) {
		this.named = named;
		this.raceColumn = raceColumn;
		this.fleet = fleet;
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
