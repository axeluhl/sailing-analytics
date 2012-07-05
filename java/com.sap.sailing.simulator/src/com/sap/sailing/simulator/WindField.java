package com.sap.sailing.simulator;

import com.sap.sailing.domain.tracking.Wind;

public interface WindField {
	
	public Wind getWind(TimedPosition coordinates);

	public Boundary getBoundaries();

}
