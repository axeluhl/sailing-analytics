package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaces;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Color;

public class FleetWithRacesImpl implements FleetWithRaces {
	private static final long serialVersionUID = -7686050535458112702L;
	
	private Fleet fleet;
	private Iterable<RaceDefinition> raceDefinitions;
	
	public FleetWithRacesImpl(
			Fleet fleet,
			Iterable<RaceDefinition> raceDefinitions) {
		this.fleet = fleet;
		this.raceDefinitions = raceDefinitions;
	}

	@Override
	public Iterable<RaceDefinition> getRaceDefinitions() {
		return raceDefinitions;
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
