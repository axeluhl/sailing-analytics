package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.RaceStatus;
import com.sap.sailing.racecommittee.app.domain.SeriesData;

public class ManagedRaceImpl implements ManagedRace {
	private static final long serialVersionUID = -4936566684992524001L;
	
	//private static final String TAG = ManagedRace.class.getName();
	
	private RaceDefinition raceDefinition;
	private Regatta regatta;
	private BoatClass boatClass;
	private SeriesData seriesData;
	private Fleet fleet;
	
	public ManagedRaceImpl(
			RaceDefinition raceDefinition, 
			Regatta regatta,
			BoatClass boatClass, 
			SeriesData seriesData, 
			Fleet fleet) {
		this.raceDefinition = raceDefinition;
		this.regatta = regatta;
		this.boatClass = boatClass;
		this.seriesData = seriesData;
		this.fleet = fleet;
	}

	public Serializable getId() {
		return raceDefinition.getId();
	}

	public String getName() {
		return raceDefinition.getName();
	}

	public Regatta getRegatta() {
		return regatta;
	}
	
	public RaceDefinition getRaceDefinition() {
		return raceDefinition;
	}

	public RaceStatus getStatus() {
		return RaceStatus.UNKNOWN;
	}

	public BoatClass getBoatClass() {
		return boatClass;
	}

	public SeriesData getSeriesData() {
		return seriesData;
	}

	public Fleet getFleet() {
		return fleet;
	}

}
