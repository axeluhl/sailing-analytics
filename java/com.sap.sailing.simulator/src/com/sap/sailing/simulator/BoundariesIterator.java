package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.Position;

public interface BoundariesIterator {
	
	enum direction {DownUp, UpDown};
	
	boolean hasUp();
	boolean hasDown();
	boolean hasLeft();
	boolean hasRight();
	
	Position Up();
	Position Down();
	Position Left();
	Position Right();
	
	double verticalStep();
	double horizontalStep();
	
	direction getDirection();

}
