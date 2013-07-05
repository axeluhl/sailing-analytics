package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreatePersistentCompetitor extends
		AbstractRacingEventServiceOperation<Competitor> {
	
	private static final long serialVersionUID = 1538753300467120249L;
//	 private static final Logger logger =
//	 Logger.getLogger(CreatePersistentCompetitor.class.getName());

	private final Serializable id;
	private final String name;
	private final Team team;
	private final Boat boat;

	public CreatePersistentCompetitor(Serializable id, String name, Team team,
			Boat boat) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.boat = boat;
	}

	@Override
	public Competitor internalApplyTo(RacingEventService toState) {
		return toState.createPersistentCompetitor(id, name, team, boat);
	}

	@Override
	public RacingEventServiceOperation<?> transformClientOp(
			RacingEventServiceOperation<?> serverOp) {
		return serverOp.transformCreatePersistentCompetitorClientOp(this);
	}

	@Override
	public RacingEventServiceOperation<?> transformServerOp(
			RacingEventServiceOperation<?> clientOp) {
		return clientOp.transformCreatePersistentCompetitorServerOp(this);
	}
}
