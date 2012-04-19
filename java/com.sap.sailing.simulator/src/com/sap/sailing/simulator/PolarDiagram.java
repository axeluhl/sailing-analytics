package com.sap.sailing.simulator;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;

public interface PolarDiagram {
		
	SpeedWithBearing getWind();
	
	void setWind(SpeedWithBearing newWind);
	
	SpeedWithBearing getSpeedAtBearing(Bearing bearing);
	
	Bearing[] optimalDirectionsUpwind();
	Bearing[] optimalDirectionsDownwind();
	
}
