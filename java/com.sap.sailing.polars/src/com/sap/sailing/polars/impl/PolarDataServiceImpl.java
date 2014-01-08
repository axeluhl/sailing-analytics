package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.caching.PolarFixCache;
import com.sap.sailing.polars.caching.PolarFixCacheRaceInterval;
import com.sap.sailing.polars.caching.PolarSheetPerBoatClassCache;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.util.SmartFutureCache;

/**
 * Uses two chained {@link SmartFutureCache}s. One to store {@link PolarFix}es extracted from {@link TrackedRace}s and
 * the other one for storing one polar sheet per boat class. This allows quick access to desired measures like optimal
 * beat angles.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarDataServiceImpl implements PolarDataService {

    private final PolarFixCache polarFixCache;
    private final PolarSheetPerBoatClassCache polarSheetPerBoatClassCache;
    private final PolarSheetAnalyzer polarSheetAnalyzer;

    public PolarDataServiceImpl(Executor executor) {
        this.polarFixCache = new PolarFixCache(executor);
        this.polarSheetPerBoatClassCache = new PolarSheetPerBoatClassCache(this);
        polarFixCache.addListener(polarSheetPerBoatClassCache);
        this.polarSheetAnalyzer = new PolarSheetAnalyzer(this);
    }

    @Override
    public SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        return polarSheetAnalyzer.getOptimalUpwindSpeedWithBearingFor(boatClass, windSpeed);
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

    @Override
    public void newRaceFinishedTracking(TrackedRace trackedRace) {
        HashSet<TrackedRace> set = new HashSet<TrackedRace>();
        set.add(trackedRace);
        polarFixCache.triggerUpdate(trackedRace.getRace().getBoatClass(), new PolarFixCacheRaceInterval(set));
    }

    @Override
    public Set<PolarFix> getPolarFixesForBoatClass(BoatClass key) {
        Map<RegattaAndRaceIdentifier, List<PolarFix>> fixesInMap = polarFixCache.get(key, false);
        Set<PolarFix> resultSet = new HashSet<PolarFix>();
        for (List<PolarFix> value : fixesInMap.values()) {
            resultSet.addAll(value);
        }
        return resultSet;
    }

    @Override
    public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass) {
        return polarSheetPerBoatClassCache.get(boatClass, false);
    }

}
