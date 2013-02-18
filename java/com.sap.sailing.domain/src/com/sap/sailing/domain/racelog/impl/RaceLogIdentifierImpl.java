package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public class RaceLogIdentifierImpl implements RaceLogIdentifier {
	
	private final Leaderboard leaderboard;
	private final RaceColumn raceColumn;
	private final Fleet fleet;

	public RaceLogIdentifierImpl(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
		this.leaderboard = leaderboard;
		this.raceColumn = raceColumn;
		this.fleet = fleet;
	}
	
	@Override
	public String getIdentifier() {
		return String.format("%s-%s-%s", leaderboard.getName(), raceColumn.getName(), fleet.getName());
	}
	
	@Override
	public String toString() {
		return getIdentifier();
	}

}
