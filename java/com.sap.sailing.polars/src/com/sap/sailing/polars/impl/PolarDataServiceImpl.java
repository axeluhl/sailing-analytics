package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.caching.NoCacheEntryException;
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

    public PolarDataServiceImpl(Executor executor) {
        this.polarFixCache = new PolarFixCache(executor);
        this.polarSheetPerBoatClassCache = new PolarSheetPerBoatClassCache(this);
        polarFixCache.addListener(polarSheetPerBoatClassCache);
    }

    @Override
    public SpeedWithConfidence<Integer> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing bearingToTheWind) {
        PolarSheetsData data = polarSheetPerBoatClassCache.get(boatClass, false);
        int windSpeedIndex = data.getStepping().getLevelIndexForValue(windSpeed.getKnots());
        double angleToTheWind = bearingToTheWind.getDegrees();
        if (angleToTheWind < 0) {
            angleToTheWind = 360 + angleToTheWind;
        }
        int angleToTheWindFloor = (int) Math.floor(bearingToTheWind.getDegrees());
        SpeedWithConfidence<Integer> speedFloor = getSpeedForConcreteValues(data, windSpeedIndex, angleToTheWindFloor);
        int angleToTheWindCeiling = (int) Math.ceil(bearingToTheWind.getDegrees());
        SpeedWithConfidence<Integer> speedCeiling = getSpeedForConcreteValues(data, windSpeedIndex,
                angleToTheWindCeiling);
        SpeedWithConfidence<Integer> speed = interpolateSpeed(speedFloor, speedCeiling, angleToTheWind
                - angleToTheWindFloor);

        return speed;
    }

    private SpeedWithConfidence<Integer> interpolateSpeed(SpeedWithConfidence<Integer> speedFloor,
            SpeedWithConfidence<Integer> speedCeiling, double distanceToFloor) {
        double distanceOverConfidenceFactor = 0.8;
        double floorFactor = distanceOverConfidenceFactor * (1 - distanceToFloor) + (1 - distanceOverConfidenceFactor)
                * speedFloor.getConfidence();
        double ceilingFactor = distanceOverConfidenceFactor * distanceToFloor + (1 - distanceOverConfidenceFactor)
                * speedCeiling.getConfidence();
        double alpha = 1 / (floorFactor + ceilingFactor);
        double scaledFactorFloor = floorFactor * alpha;
        double scaledFactorCeiling = ceilingFactor * alpha;
        // FIXME remove this assert when testing is done
        assert (1 - (scaledFactorCeiling + scaledFactorFloor) < 0.005 && 1 - (scaledFactorCeiling + scaledFactorFloor) > -0.005);
        double interpolatedSpeedInKnots = scaledFactorFloor * speedFloor.getObject().getKnots() + scaledFactorCeiling
                * speedCeiling.getObject().getKnots();
        double interpolatedConfidence = scaledFactorFloor * speedFloor.getConfidence() + scaledFactorCeiling
                * speedCeiling.getConfidence();
        Speed interpolatedSpeed = new KnotSpeedImpl(interpolatedSpeedInKnots);
        int numberOfUnderlyingFixes = speedFloor.getRelativeTo() + speedCeiling.getRelativeTo();
        return new SpeedWithConfidenceImpl<Integer>(interpolatedSpeed, interpolatedConfidence, numberOfUnderlyingFixes);
    }

    private SpeedWithConfidence<Integer> getSpeedForConcreteValues(PolarSheetsData data, int windSpeedIndex,
            int angleToTheWind) {
        Number rawSpeed = data.getAveragedPolarDataByWindSpeed()[windSpeedIndex][angleToTheWind];
        Speed speed = new KnotSpeedImpl(rawSpeed.doubleValue());
        PolarSheetsHistogramData histogramData = data.getHistogramDataMap().get(windSpeedIndex).get(angleToTheWind);
        double confidence = histogramData.getConfidenceMeasure();
        int numberOfUnderlyingFixes = histogramData.getDataCount();
        SpeedWithConfidence<Integer> speedWithConfidence = new SpeedWithConfidenceImpl<Integer>(speed, confidence,
                numberOfUnderlyingFixes);
        return speedWithConfidence;
    }


    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        Set<PolarFix> fixes;
        // If settings are default, look into cache
        if (settings.areDefault()) {
            try {
                fixes = polarFixCache.getFixesForTrackedRaces(trackedRaces);
            } catch (NoCacheEntryException e) {
                // If there is no cache entry for at least one of the races: Aggregate for non-cached races.
                fixes = e.getCachedResultList();
                PolarFixAggregator aggregator = new PolarFixAggregator(e.getNotCached(), settings, executor);
                aggregator.startPolarFixAggregation();
                fixes.addAll(aggregator.get());
            }
        } else {
            PolarFixAggregator aggregator = new PolarFixAggregator(trackedRaces, settings, executor);
            aggregator.startPolarFixAggregation();
            fixes = aggregator.get();
        }
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

    @Override
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
        return polarSheetPerBoatClassCache.keySet();
    }

}
