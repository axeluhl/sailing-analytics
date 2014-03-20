package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.NoPolarDataAvailableException;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.analysis.impl.PolarSheetAnalyzerImpl;
import com.sap.sailing.polars.caching.NoCacheEntryException;
import com.sap.sailing.polars.caching.PolarFixCache;
import com.sap.sailing.polars.caching.PolarSheetPerBoatClassCache;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.mining.PolarDataMiner;
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

    private static final Logger logger = Logger.getLogger(PolarDataServiceImpl.class.getName());

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
            throws NoPolarDataAvailableException {
        PolarSheetsData data = polarSheetPerBoatClassCache.get(boatClass, false);
        double angleToTheWind = bearingToTheWind.getDegrees();
        if (angleToTheWind < 0) {
            angleToTheWind = 360 + angleToTheWind;
        }
        int angleToTheWindFloor = (int) Math.floor(bearingToTheWind.getDegrees());
        int angleToTheWindCeiling = (int) Math.ceil(bearingToTheWind.getDegrees());
        int windSpeedIndexFloor = data.getStepping().getLevelIndexFloorForValue(windSpeed.getKnots());
        boolean useFloor = true;
        boolean useCeiling = true;
        int windSpeedIndexCeiling;
        if (windSpeedIndexFloor == -1) {
            useFloor = false;
            windSpeedIndexCeiling = 0;
        } else if (windSpeedIndexFloor == data.getStepping().getRawStepping().length - 1) {
            windSpeedIndexCeiling = -1;
            useCeiling = false;
        } else {
            windSpeedIndexCeiling = data.getStepping().getLevelIndexCeilingForValue(windSpeed.getKnots());
        }
        SpeedWithConfidence<Integer> speed;
        if (useFloor && useCeiling) {
            double windSpeedIndexDistanceToFloor = data.getStepping().getDistanceToLevelFloor(windSpeed.getKnots());
            SpeedWithConfidence<Integer> windSpeedFloor = getInterpolatedSpeedBetweenAngles(boatClass, data,
                    windSpeedIndexFloor, angleToTheWind, angleToTheWindFloor, angleToTheWindCeiling);
            SpeedWithConfidence<Integer> windSpeedCeiling = getInterpolatedSpeedBetweenAngles(boatClass, data,
                    windSpeedIndexCeiling, angleToTheWind, angleToTheWindFloor, angleToTheWindCeiling);
            speed = interpolateSpeed(windSpeedFloor, windSpeedCeiling, windSpeedIndexDistanceToFloor);
        } else if (useFloor) {
            speed = getInterpolatedSpeedBetweenAngles(boatClass, data, windSpeedIndexFloor, angleToTheWind,
                    angleToTheWindFloor, angleToTheWindCeiling);
        } else {
            speed = getInterpolatedSpeedBetweenAngles(boatClass, data, windSpeedIndexCeiling, angleToTheWind,
                    angleToTheWindFloor, angleToTheWindCeiling);
        }

        return speed;
    }

    private SpeedWithConfidence<Integer> getInterpolatedSpeedBetweenAngles(BoatClass boatClass, PolarSheetsData data,
            int windSpeedIndex, double angleToTheWind, int angleToTheWindFloor, int angleToTheWindCeiling)
            throws NoPolarDataAvailableException {
        SpeedWithConfidence<Integer> speedFloor = getSpeedForConcreteValues(data, windSpeedIndex, angleToTheWindFloor,
                boatClass);
        SpeedWithConfidence<Integer> speedCeiling = getSpeedForConcreteValues(data, windSpeedIndex,
                angleToTheWindCeiling, boatClass);
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
            int angleToTheWind, BoatClass boatClass) throws NoPolarDataAvailableException {
        Number rawSpeed = data.getAveragedPolarDataByWindSpeed()[windSpeedIndex][angleToTheWind];
        Speed speed = new KnotSpeedImpl(rawSpeed.doubleValue());
        PolarSheetsHistogramData histogramData = data.getHistogramDataMap().get(windSpeedIndex).get(angleToTheWind);
        SpeedWithConfidenceImpl<Integer> speedWithConfidence;
        if (histogramData != null) {
        double confidence = histogramData.getConfidenceMeasure();
        int numberOfUnderlyingFixes = histogramData.getDataCount();
            speedWithConfidence = new SpeedWithConfidenceImpl<Integer>(speed, confidence,
                numberOfUnderlyingFixes);
        } else {
            NoPolarDataAvailableException e = new NoPolarDataAvailableException(boatClass, new KnotSpeedImpl(data
                    .getStepping().getRawStepping()[windSpeedIndex]), new DegreeBearingImpl(angleToTheWind));
            logger.warning(e.getLocalizedMessage());
            throw e;
        }
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
                PolarFixAggregator aggregator = new PolarFixAggregator(
                        new SimplePolarFixRaceInterval(e.getNotCached()),
                        settings, executor);
                aggregator.startPolarFixAggregation();
                fixes.addAll(aggregator.getAggregationResultAsSingleList());
            }
        } else {
            PolarFixAggregator aggregator = new PolarFixAggregator(new SimplePolarFixRaceInterval(trackedRaces),
                    settings, executor);
            aggregator.startPolarFixAggregation();
            fixes = aggregator.getAggregationResultAsSingleList();
        }
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
        return polarSheetPerBoatClassCache.get(boatClass, false);
    }

    @Override
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
        return polarSheetPerBoatClassCache.keySet();
    }

    @Override
    public PolarSheetAnalyzer getAnalyzer() {
        return polarSheetAnalyzer;
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
