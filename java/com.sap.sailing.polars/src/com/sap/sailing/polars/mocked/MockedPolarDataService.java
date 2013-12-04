package com.sap.sailing.polars.mocked;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.polars.PolarDataService;

public class MockedPolarDataService implements PolarDataService {

    @Override
    public SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing getOptimalDownwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getOptimalReachingSpeedFor(BoatClass boatClass, Speed windSpeed, Bearing bearingDifferenceToWind) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing getAverageUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing getAverageDownwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageReachingSpeedFor(BoatClass boatClass, Speed windSpeed, Bearing bearingDifferenceToWind) {
        // TODO Auto-generated method stub
        return null;
    }

}
