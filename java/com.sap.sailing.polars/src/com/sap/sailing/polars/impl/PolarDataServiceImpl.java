package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.analysis.impl.PolarSheetAnalyzerImpl;
import com.sap.sailing.polars.caching.PolarFixCache;
import com.sap.sailing.polars.caching.PolarSheetPerBoatClassCache;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
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

    private final PolarDataMiner polarDataMiner;

    public PolarDataServiceImpl(Executor executor) {
        this.polarFixCache = new PolarFixCache(executor);
        this.polarSheetPerBoatClassCache = new PolarSheetPerBoatClassCache(this);
        polarFixCache.addListener(polarSheetPerBoatClassCache);
        this.polarSheetAnalyzer = new PolarSheetAnalyzerImpl(this);
        this.polarDataMiner = new PolarDataMiner();
    }

    @Override
    public SpeedWithConfidence<Integer> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing bearingToTheWind)
            throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.estimateBoatSpeed(boatClass, windSpeed, bearingToTheWind);
    }
    
    @Override
    public SpeedWithBearing getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        return polarSheetAnalyzer.getAverageUpwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
    }

    @Override
    public SpeedWithBearing getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        return polarSheetAnalyzer.getAverageDownwindSpeedWithBearingOnStarboardTackFor(boatClass, windSpeed);
    }

    @Override
    public SpeedWithBearing getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        return polarSheetAnalyzer.getAverageUpwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
    }

    @Override
    public SpeedWithBearing getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        return polarSheetAnalyzer.getAverageDownwindSpeedWithBearingOnPortTackFor(boatClass, windSpeed);
    }


    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        Set<PolarFix> fixes;
        // If settings are default, look into cache
        // if (settings.areDefault()) {
        // try {
        // fixes = polarFixCache.getFixesForTrackedRaces(trackedRaces);
        // } catch (NoCacheEntryException e) {
        // // If there is no cache entry for at least one of the races: Aggregate for non-cached races.
        // fixes = e.getCachedResultList();
        // PolarFixAggregator aggregator = new PolarFixAggregator(
        // new SimplePolarFixRaceInterval(e.getNotCached()),
        // settings, executor);
        // aggregator.startPolarFixAggregation();
        // fixes.addAll(aggregator.getAggregationResultAsSingleList());
        // }
        // } else {
        PolarFixAggregator aggregator = new PolarFixAggregator(new SimplePolarFixRaceInterval(trackedRaces), settings,
                executor);
        aggregator.startPolarFixAggregation();
        fixes = aggregator.getAggregationResultAsSingleList();
        // }
        PolarSheetGenerator generator = new PolarSheetGenerator(fixes, settings);
        return generator.generate();
    }

    @Override
    public void newRaceFinishedTracking(TrackedRace trackedRace) {
        // TODO add a similar listening method, that deletes all cached data on removal of race
        // HashSet<TrackedRace> set = new HashSet<TrackedRace>();
        // set.add(trackedRace);
        // polarFixCache.triggerUpdate(trackedRace.getRace().getBoatClass(), new PolarFixCacheRaceInterval(set));
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
        return polarDataMiner.createFullSheetForBoatClass(boatClass);
        //return polarSheetPerBoatClassCache.get(boatClass, false);
    }

    @Override
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
        return polarDataMiner.getAvailableBoatClasses();
        //return polarSheetPerBoatClassCache.keySet();
    }

    @Override
    public void competitorPositionChanged(final GPSFixMoving fix, final Competitor competitor,
            final TrackedRace createdTrackedRace) {
        polarDataMiner.addFix(fix, competitor, createdTrackedRace);
        // TODO build datamining pipeline for the cache

        // PolarFixCacheRaceInterval interval = new PolarFixCacheRaceInterval(createIntervalSpecification(
        // createdTrackedRace, competitor, timePoint));
        // polarFixCache.triggerUpdate(createdTrackedRace.getRace().getBoatClass(), interval);

    }

    // private Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> createIntervalSpecification(
    // TrackedRace createdTrackedRace, Competitor competitor, TimePoint timePoint) {
    // HashMap<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> result = new HashMap<TrackedRace,
    // Map<Competitor, Pair<TimePoint, TimePoint>>>();
    // HashMap<Competitor, Pair<TimePoint, TimePoint>> competitorMap = new HashMap<Competitor, Pair<TimePoint,
    // TimePoint>>();
    // competitorMap.put(competitor, new Pair<TimePoint, TimePoint>(timePoint, timePoint));
    // result.put(createdTrackedRace, competitorMap);
    // return result;
    // }

}
