package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.TimedPosition;

public class TimedPositionImpl implements TimedPosition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -675796846985362731L;
	
	TimePoint timePoint;
	Position position;
	
	public TimedPositionImpl(TimePoint tp, Position p) {
		timePoint = tp;
		position = p;
	}
	
	@Override
	public TimePoint getTimePoint() {
		return timePoint;
	}

	@Override
	public Position getPosition() {
		return position;
	}

}
