package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceColumnIdentifier;

public class RaceColumnIdentifierImpl implements RaceColumnIdentifier {
	
	private String leaderboardName;
	private String columnName;

	public RaceColumnIdentifierImpl(String leaderboardName, String columnName) {
		this.leaderboardName = leaderboardName;
		this.columnName = columnName;
	}

	@Override
	public String getIdentifier() {
		return String.format("%s-%s", leaderboardName, columnName);
	}

}
