package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesData;

public class SeriesDataImpl implements SeriesData {
	private static final long serialVersionUID = 8825402393444809944L;
	
	private String name;
	private Iterable<? extends Fleet> fleets;
	private boolean isMedal;

	public SeriesDataImpl(String name, Iterable<? extends Fleet> fleets, boolean isMedal) {
		this.name = name;
		this.fleets = fleets;
		this.isMedal = isMedal;
	}

	public String getName() {
		return name;
	}

	public Iterable<? extends Fleet> getFleets() {
		return fleets;
	}

	public boolean isMedal() {
		return isMedal;
	}

}
