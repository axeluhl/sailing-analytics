package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;

public interface BoundariesIterator {
	
	boolean hasNext();
	boolean hasUp();
	boolean hasDown();
	boolean hasRight();
	boolean hasLeft();

	Position next() throws Exception;
	Position up() throws Exception;
	Position down() throws Exception;
	Position right() throws Exception;
	Position left() throws Exception;
	
	Distance getHorizontalStep();
	Distance getVerticalStep();
	
	void setVerticalStep(Distance newVerticalStep);
	void setHorizontalStep(Distance newHorizontalStep);
	
	void setHorizontalResolution(double xRes);
	void setVerticalResolution(double yRes);
	
	Bearing getVerticalBearing();
	Bearing getHorizontalBearing();
	
	void setVerticalBearing(Bearing newVerticalBearing);
	void setHorizontalBearing(Bearing newHorizontalBearing);
	
	void reset();

}
