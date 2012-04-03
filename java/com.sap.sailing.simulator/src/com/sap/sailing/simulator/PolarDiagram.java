package com.sap.sailing.simulator;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;

public interface PolarDiagram {
	Wind getWind();
	
	void setWind(Wind newWind);
	
	SpeedWithBearing getSpeedAtBearing(Bearing bearing);
	
	SpeedWithBearing portBowMaxSpeed();
	SpeedWithBearing starboardBowMaxSpeed();
	SpeedWithBearing starboardQuarterMaxSpeed();
	SpeedWithBearing portQuarterMaxSpeed();

}
