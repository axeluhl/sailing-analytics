package com.sap.sailing.polars.impl;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.util.SmartFutureCache;

/**
 * Uses two chained {@link SmartFutureCache}s. One to store {@link PolarFix}es extracted from {@link TrackedRace}s and
 * the other one for storing one polar sheet per boat class. This enables quick access to desired measures like optimal
 * beat angles.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarDataServiceImpl implements PolarDataService {

    private final PolarDataMiner polarDataMiner;

    public PolarDataServiceImpl(Executor executor) {
        this.polarDataMiner = new PolarDataMiner();
    }

    @Override
    public SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing trueWindAngle) throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.estimateBoatSpeed(boatClass, windSpeed, trueWindAngle);
    }
    
    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass,
            Speed windSpeed, LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        SpeedWithBearingWithConfidence<Void> speedWithBearing = null;
        if (tack.equals(Tack.STARBOARD)) {
            if (legType.equals(LegType.UPWIND)) {
                speedWithBearing = polarDataMiner.getAverageUpwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
            } else if (legType.equals(LegType.DOWNWIND)) {
                speedWithBearing = polarDataMiner.getAverageDownwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
            }
        } else if (tack.equals(Tack.PORT)) {
            if (legType.equals(LegType.UPWIND)) {
                return polarDataMiner.getAverageUpwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
            } else if (legType.equals(LegType.DOWNWIND)) {
                return polarDataMiner.getAverageDownwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
            }
        }
        return speedWithBearing;
    }


    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        Set<PolarFix> fixes;
        PolarFixAggregator aggregator = new PolarFixAggregator(new SimplePolarFixRaceInterval(trackedRaces), settings,
                executor);
        aggregator.startPolarFixAggregation();
        fixes = aggregator.getAggregationResultAsSingleList();
        PolarSheetGenerator generator = new PolarSheetGenerator(fixes, settings);
        return generator.generate();
    }

    @Override
    public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass) {
        return polarDataMiner.createFullSheetForBoatClass(boatClass);
    }

    @Override
    public Set<BoatClassMasterdata> getAllBoatClassesWithPolarSheetsAvailable() {
        return polarDataMiner.getAvailableBoatClasses();
    }

    @Override
    public void competitorPositionChanged(final GPSFixMoving fix, final Competitor competitor,
            final TrackedRace createdTrackedRace) {
        polarDataMiner.addFix(fix, competitor, createdTrackedRace);
    }

    @Override
    public int[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive) {
        return polarDataMiner.getDataCountsForWindSpeed(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
    }

}
