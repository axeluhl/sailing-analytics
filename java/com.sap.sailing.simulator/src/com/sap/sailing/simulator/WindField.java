package com.sap.sailing.simulator;

import com.sap.sailing.domain.tracking.Wind;

public interface WindField {
	
	Wind getWind(TimedPosition coordinates);
	Boundary getBoundaries();

}
