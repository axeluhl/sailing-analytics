package com.sap.sailing.racecommittee.app.domain;

import java.io.Serializable;

import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;

public interface FleetIdentifier {

	public FleetWithRaceNames getFleet(); 
	
	public SeriesData getSeries();
	
	public RaceGroup getRaceGroup();

	public Serializable getId();
}
