package com.sap.sailing.simulator;

import com.sap.sailing.domain.base.SpeedWithBearing;

public interface TimedPositionWithSpeed extends TimedPosition {

	SpeedWithBearing getSpeed();
	
}
