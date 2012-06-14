package com.sap.sailing.simulator;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;

public interface PolarDiagram {
		
	enum WindSide {RIGHT, LEFT, FACING, OPPOSING};
	
	SpeedWithBearing getWind();
	
	void setWind(SpeedWithBearing newWind);
	
	SpeedWithBearing getSpeedAtBearing(Bearing bearing);
	
	Bearing[] optimalDirectionsUpwind();
	Bearing[] optimalDirectionsDownwind();
	
	long getTurnLoss();
	WindSide getWindSide();
	
	
}
