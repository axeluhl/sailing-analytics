package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.base.RaceRow;

public class RaceRowImpl implements RaceRow {

	private Fleet fleet;
	private Iterable<RaceCell> races;
	
	public RaceRowImpl(Fleet fleet, Iterable<RaceCell> races) {
		this.fleet = fleet;
		this.races = races;
	}

	@Override
	public Fleet getFleet() {
		return fleet;
	}

	@Override
	public Iterable<RaceCell> getCells() {
		return races;
	}

}
