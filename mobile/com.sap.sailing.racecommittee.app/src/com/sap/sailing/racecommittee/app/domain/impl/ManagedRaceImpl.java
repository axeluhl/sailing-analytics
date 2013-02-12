package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceState;
import com.sap.sailing.racecommittee.app.domain.RaceGroup;

public class ManagedRaceImpl implements ManagedRace {
	private static final long serialVersionUID = -4936566684992524001L;
	
	//private static final String TAG = ManagedRace.class.getName();
	
	private ManagedRaceIdentifier identifier;	
	private ManagedRaceState state;
	
	public ManagedRaceImpl(ManagedRaceIdentifier identifier, RaceLog raceLog) {
		this(identifier, new ManagedRaceStateImpl(raceLog));
	}
	
	public ManagedRaceImpl(
			ManagedRaceIdentifier identifier,
			ManagedRaceState state) {
		this.identifier = identifier;
		this.state = state;
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

	public Fleet getFleet() {
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

	public ManagedRaceState getState() {
		return state;
	}

	public RaceLog getRaceLog() {
		return state.getRaceLog();
	}
	
	public RaceLogRaceStatus getStatus() {
		return state.getStatus();
	}

}
