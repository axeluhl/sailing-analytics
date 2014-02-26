package com.sap.sailing.polars.analysis;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;

public interface PolarSheetAnalyzerInterface {

	SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass,
			Speed windSpeed);

	SpeedWithBearing getOptimalDownwindSpeedWithBearingFor(BoatClass boatClass,
			Speed windSpeed);

}