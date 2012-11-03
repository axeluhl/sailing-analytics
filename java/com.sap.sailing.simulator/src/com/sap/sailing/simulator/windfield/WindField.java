package com.sap.sailing.simulator.windfield;

import java.io.Serializable;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPosition;

public interface WindField extends Serializable {
	
	public Wind getWind(TimedPosition coordinates);

	public Boundary getBoundaries();
	
	public Path getLine(TimedPosition seed);

}
