package com.sap.sailing.polars.analysis;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public interface PolarSheetAnalyzer {

	SpeedWithBearing getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearing getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed) throws NotEnoughDataHasBeenAddedException;
	
	SpeedWithBearing getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearing getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed) throws NotEnoughDataHasBeenAddedException;

}