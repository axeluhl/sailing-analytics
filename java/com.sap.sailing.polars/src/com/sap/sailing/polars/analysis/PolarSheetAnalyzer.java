package com.sap.sailing.polars.analysis;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public interface PolarSheetAnalyzer {

	SpeedWithBearing getOptimalUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearing getOptimalDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed) throws NotEnoughDataHasBeenAddedException;
	
	SpeedWithBearing getOptimalUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearing getOptimalDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

}