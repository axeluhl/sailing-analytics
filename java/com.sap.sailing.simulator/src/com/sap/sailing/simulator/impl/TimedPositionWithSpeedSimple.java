package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class TimedPositionWithSpeedSimple implements TimedPositionWithSpeed {

	/**
	 * 
	 */
	private static final long serialVersionUID = 73150541143821154L;

	Position position;
	
	public TimedPositionWithSpeedSimple(Position p) {
		position = p;
	}
	
	@Override
	public TimePoint getTimePoint() {
		return null;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public SpeedWithBearing getSpeed() {
		return (SpeedWithBearing) Speed.NULL;
	}

}
