package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;

public interface BoundariesIterator {
	
	boolean hasNext();

	Position next();
	
	Distance getHorizontalStep();
	Distance getVerticalStep();
	
	void setVerticalStep(Distance newVerticalStep);
	void setHorizontalStep(Distance newHorizontalStep);
	
	void setHorizontalResolution(double xRes);
	void setVerticalResolution(double yRes);
	
	void reset();

}
