package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

public interface ManagedRace extends Named, WithID {

	public RaceStatus getStatus();
	
	public Regatta getRegatta();
	
	public RaceDefinition getRaceDefinition();
	
	public BoatClass getBoatClass();
	
	public SeriesData getSeriesData();
	
	public Fleet getFleet();
	
}
