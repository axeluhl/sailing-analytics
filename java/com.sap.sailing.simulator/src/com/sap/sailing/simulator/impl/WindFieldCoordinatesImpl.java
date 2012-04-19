package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.WindFieldCoordinates;

public class WindFieldCoordinatesImpl implements WindFieldCoordinates {

	private Position position;
	
	public WindFieldCoordinatesImpl(Position p) {
		
		position = p;
	}
	
	@Override
	public Position getPosition() {

		return position;
	}

	@Override
	public TimePoint getTimePoint() {
		
		return null;
		
	}

}
