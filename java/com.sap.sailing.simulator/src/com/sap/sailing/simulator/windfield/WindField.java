package com.sap.sailing.simulator.windfield;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPosition;

public interface WindField {
	
	public Wind getWind(TimedPosition coordinates);

	public Boundary getBoundaries();
	
	public Path getLine(TimedPosition seed);

}
