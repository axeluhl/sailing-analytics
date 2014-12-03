package com.sap.sailing.polars.analysis;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public interface PolarSheetAnalyzer {

        SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed, boolean useLinReg) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass,
			Speed windSpeed, boolean useLinReg) throws NotEnoughDataHasBeenAddedException;
	
	SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed, boolean useLinReg) throws NotEnoughDataHasBeenAddedException;

	SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass,
                Speed windSpeed, boolean useLinReg) throws NotEnoughDataHasBeenAddedException;

}