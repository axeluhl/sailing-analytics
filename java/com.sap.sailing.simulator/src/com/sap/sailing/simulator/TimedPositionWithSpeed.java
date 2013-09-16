package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.SpeedWithBearing;

public interface TimedPositionWithSpeed extends TimedPosition {

	SpeedWithBearing getSpeed();
	
}
