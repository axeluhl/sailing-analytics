package com.sap.sailing.simulator.windfield;

import java.io.Serializable;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPosition;

public interface WindField extends Serializable {
	
	public Wind getWind(TimedPosition coordinates);

	public Grid getGrid();
	
	public Path getLine(TimedPosition seed, boolean forward);

}
