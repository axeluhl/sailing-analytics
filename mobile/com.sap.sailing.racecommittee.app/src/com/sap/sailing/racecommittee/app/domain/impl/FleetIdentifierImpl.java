package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;

public class FleetIdentifierImpl implements FleetIdentifier {

	private Fleet fleet;
	private SeriesBase series;
	private RaceGroup raceGroup;
	
	public FleetIdentifierImpl(
			Fleet fleet,
			SeriesBase series, 
			RaceGroup raceGroup) {
		this.fleet = fleet;
		this.series = series;
		this.raceGroup = raceGroup;
	}

    public FleetIdentifierImpl(Fleet fleet, SeriesData series, RaceGroup raceGroup) {
        this.fleet = fleet;
        this.series = series;
        this.raceGroup = raceGroup;
    }

	public SeriesBase getSeries() {
		return series;
	}
    public Fleet getFleet() {
        return fleet;
    }

    public SeriesData getSeries() {
        return series;
    }

    public RaceGroup getRaceGroup() {
        return raceGroup;
    }

    public Serializable getId() {
        return String.format("%s.%s.%s", getRaceGroup().getName(), getSeries().getName(), getFleet().getName());
    }

}
