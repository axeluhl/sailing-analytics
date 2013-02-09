package com.sap.sailing.racecommittee.app.domain;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesData;

public interface FleetIdentifier {

	public Fleet getFleet(); 
	
	public SeriesData getSeries();
	
	public RaceGroup getRaceGroup();

	public Serializable getId();
}
