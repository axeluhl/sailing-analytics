package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEvent extends AbstractRacingEventServiceOperation<Void> {

	private static final long serialVersionUID = -2532392938428607006L;
	private final String leaderboardOrRegattaName;
	private final String raceColumnName;
	private final RaceLogEvent event;

	public RecordRaceLogEvent(String name, String raceColumnName, RaceLogEvent event) {
		super();
		this.leaderboardOrRegattaName = name;
		this.raceColumnName = raceColumnName;
		this.event = event;
	}

	@Override
	public Void internalApplyTo(RacingEventService toState) throws Exception {
//		DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
//		trackedRace.recordRaceLogEvent(event);
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
