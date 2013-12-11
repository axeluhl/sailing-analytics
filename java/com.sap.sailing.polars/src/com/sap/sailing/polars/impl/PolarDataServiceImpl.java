package com.sap.sailing.polars.impl;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;

public class PolarDataServiceImpl implements PolarDataService {

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

    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        PolarFixAggregator aggregator = new PolarFixAggregator(trackedRaces, settings, executor);
        aggregator.startPolarFixAggregation();
        Set<PolarFix> fixes = aggregator.get();
        PolarSheetGenerator generator = new PolarSheetGenerator(fixes, settings);
        return generator.generate();
    }

}
