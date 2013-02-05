package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.RaceStatus;

public class ManagedRaceImpl implements ManagedRace {
	private static final long serialVersionUID = -4936566684992524001L;
	
	//private static final String TAG = ManagedRace.class.getName();

	private ManagedRaceIdentifier identifier;
	
	public ManagedRaceImpl(ManagedRaceIdentifier identifier) {
		this.identifier = identifier;
	}

	public Serializable getId() {
		return identifier.getId();
	}

	public String getName() {
		return identifier.getRaceName();
	}

	public String getRaceName() {
		return getName();
	}

	public FleetWithRaceNames getFleet() {
		return identifier.getFleet();
	}

	public SeriesData getSeries() {
		return identifier.getSeries();
	}

	public RaceGroup getRaceGroup() {
		return identifier.getRaceGroup();
	}

	public ManagedRaceIdentifier getIdentifier() {
		return identifier;
	}

	public RaceStatus getStatus() {
		return RaceStatus.UNKNOWN;
	}

}
