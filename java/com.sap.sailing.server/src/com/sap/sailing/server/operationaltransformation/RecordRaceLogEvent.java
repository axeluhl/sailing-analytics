package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordRaceLogEvent extends AbstractRacingEventServiceOperation<Void> {

	private static final long serialVersionUID = -2532392938428607006L;
	private final String leaderboardName;
	private final String raceColumnName;
	private final String fleetName;
	private final RaceLogEvent event;

	public RecordRaceLogEvent(String leaderboardName, String raceColumnName, String fleetName, RaceLogEvent event) {
		super();
		this.leaderboardName = leaderboardName;
		this.raceColumnName = raceColumnName;
		this.fleetName = fleetName;
		this.event = event;
	}

	@Override
	public Void internalApplyTo(RacingEventService toState) throws Exception {
		Leaderboard leaderboard = toState.getLeaderboardByName(leaderboardName);
		RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
		Fleet fleet = raceColumn.getFleetByName(fleetName);
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
