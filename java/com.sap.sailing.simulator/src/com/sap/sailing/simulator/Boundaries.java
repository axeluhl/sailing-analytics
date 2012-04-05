package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.BoundariesIterator;

public interface Boundaries {
	
	Position[] getCorners();
	
	boolean isWithinBoundaries(Position P);
	
	BoundariesIterator boundariesIterator();

}
