package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;

public interface RaceRow {
	
	Fleet getFleet();
	
	Iterable<RaceCell> getCells();

}
