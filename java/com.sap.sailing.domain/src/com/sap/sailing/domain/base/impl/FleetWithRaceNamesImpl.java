package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.common.Color;

public class FleetWithRaceNamesImpl implements FleetWithRaceNames {
	private static final long serialVersionUID = -7686050535458112702L;
	
	private Fleet fleet;
	private Iterable<String> raceNames;
	
	public FleetWithRaceNamesImpl(
			Fleet fleet,
			Iterable<String> raceNames) {
		this.fleet = fleet;
		this.raceNames = raceNames;
	}

	@Override
	public Iterable<String> getRaceNames() {
		return raceNames;
	}

	@Override
	public String getName() {
		return fleet.getName();
	}

	@Override
	public int compareTo(Fleet otherFleet) {
		return fleet.compareTo(otherFleet);
	}

	@Override
	public int getOrdering() {
		return fleet.getOrdering();
	}

	@Override
	public Color getColor() {
		return fleet.getColor();
	}

}
