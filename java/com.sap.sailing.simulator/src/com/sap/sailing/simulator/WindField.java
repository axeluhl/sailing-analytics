package com.sap.sailing.simulator;

import com.sap.sailing.simulator.WindFieldCoordinates;
import com.sap.sailing.domain.tracking.Wind;

public interface WindField {
	
	Wind getWind(WindFieldCoordinates coordinates);
	Boundaries getBoundaries();

}
