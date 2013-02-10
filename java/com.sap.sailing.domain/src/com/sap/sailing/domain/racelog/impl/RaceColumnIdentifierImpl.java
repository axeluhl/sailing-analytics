package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;

public class RaceColumnIdentifierImpl implements RaceColumnIdentifier {
	
	private Leaderboard leaderboard;
	private String columnName;

	public RaceColumnIdentifierImpl(Leaderboard leaderboard, String columnName) {
		this.leaderboard = leaderboard;
		this.columnName = columnName;
	}

	@Override
	public String getIdentifier() {
		return String.format("%s %s", leaderboard.getName(), columnName);
	}

}
