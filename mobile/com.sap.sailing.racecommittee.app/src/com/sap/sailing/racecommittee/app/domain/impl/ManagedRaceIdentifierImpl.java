package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;

public class ManagedRaceIdentifierImpl implements ManagedRaceIdentifier {

	private String raceName;
	private FleetWithRaceNames fleetWithRaceNames;
	private SeriesData series;
	private RaceGroup raceGroup;
	
	public ManagedRaceIdentifierImpl(String raceName,
			FleetWithRaceNames fleetWithRaceNames, SeriesData series,
			RaceGroup raceGroup) {
		this.raceName = raceName;
		this.fleetWithRaceNames = fleetWithRaceNames;
		this.series = series;
		this.raceGroup = raceGroup;
	}

	public String getRaceName() {
		return raceName;
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
		return String.format("%s.%s.%s.%s", 
				getRaceGroup().getName(),
				getSeries().getName(),
				getFleet().getName(),
				getRaceName());
	}

}
