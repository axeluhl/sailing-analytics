package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;

public class FleetIdentifierImpl implements FleetIdentifier {

	private FleetWithRaceNames fleetWithRaceNames;
	private SeriesData series;
	private RaceGroup raceGroup;
	
	public FleetIdentifierImpl(
			FleetWithRaceNames fleetWithRaceNames,
			SeriesData series, 
			RaceGroup raceGroup) {
		this.fleetWithRaceNames = fleetWithRaceNames;
		this.series = series;
		this.raceGroup = raceGroup;
	}

	public FleetWithRaceNames getFleet() {
		return fleetWithRaceNames;
	}

	public SeriesData getSeries() {
		return series;
	}

	public RaceGroup getRaceGroup() {
		return raceGroup;
	}

	public Serializable getId() {
		return String.format("%s.%s.%s", 
				getRaceGroup().getName(),
				getSeries().getName(),
				getFleet().getName());
	}

}
