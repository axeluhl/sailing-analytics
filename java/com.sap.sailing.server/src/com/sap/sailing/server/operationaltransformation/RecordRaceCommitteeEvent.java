package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceCommitteeEvent extends AbstractRaceOperation<Void> {

	private static final long serialVersionUID = -2532392938428607006L;
	private final RaceCommitteeEvent event;

	public RecordRaceCommitteeEvent(RegattaAndRaceIdentifier raceIdentifier, RaceCommitteeEvent event) {
		super(raceIdentifier);
		this.event = event;
	}

	@Override
	public Void internalApplyTo(RacingEventService toState) throws Exception {
		DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
		trackedRace.recordRaceCommitteeEvent(event);
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
