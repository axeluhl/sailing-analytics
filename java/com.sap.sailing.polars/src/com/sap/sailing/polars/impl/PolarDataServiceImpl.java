package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.SmartFutureCache;

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
    public Set<SpeedWithBearingWithConfidence<Void>> getAverageTrueWindSpeedAndAngleCandidates(BoatClass boatClass,
            Speed speedOverGround, LegType legType, Tack tack) {
        
        // The following is an estimation function. It only serves as a stub. It's the same for all boatclasses and returns
        // default maneuver angles. This is NOT final.
        // The function is able to return boat speed values for windspeed alues between 5kn and 25kn , which are some kind of realistic
        // for sailing boats. They are taken from the 505 polars we gathered in the races until now.
        // TODO keep a cache of sampling points over which to fit a function for every boat class and then return the
        // corresponding angle

        Set<SpeedWithBearingWithConfidence<Void>> resultSet = new HashSet<>();
        final int tackFactor = (tack.equals(Tack.PORT)) ? -1 : 1;
        if (legType.equals(LegType.UPWIND)) {
            CubicEquation upWindEquation = new CubicEquation(0.0002, -0.0245, 0.7602, -0.0463-speedOverGround.getKnots());
            int angle = 49 * tackFactor;
            solveAndAddResults(resultSet, upWindEquation, angle);
        } else if (legType.equals(LegType.DOWNWIND)) {
            CubicEquation downWindEquation = new CubicEquation(0.0003, -0.0373, 1.5213, -2.1309-speedOverGround.getKnots());
            int angle = 30 * tackFactor;
            solveAndAddResults(resultSet, downWindEquation, angle);
        }
        return resultSet;
        //return polarDataMiner.estimateTrueWindSpeedAndAngleCandidates(boatClass, speedOverGround, legType, tack);
    }

    private void solveAndAddResults(Set<SpeedWithBearingWithConfidence<Void>> result, CubicEquation equation, int angle) {
        double[] windSpeedCandidates = equation.solve();
        for (int i = 0; i < windSpeedCandidates.length; i++) {
            double windSpeedCandidateInKnots = windSpeedCandidates[i] > 0 ? windSpeedCandidates[i] : 0;
            if (windSpeedCandidateInKnots < 40) {
                result.add(new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(
                        windSpeedCandidateInKnots, new DegreeBearingImpl(angle)), 0.00001, null));
            }
        }
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass,
            Speed windSpeed, LegType legType, Tack tack, boolean useRegressionForSpeed) throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.getAverageSpeedAndCourseOverGround(boatClass, windSpeed, legType, tack, useRegressionForSpeed);
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
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
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

    @Override
    public double getConfidenceForTackJibeSpeedRatio(Speed intoTackSpeed, Speed intoJibeSpeed, BoatClass boatClass) {
        return Math.min(1., 0.5*intoJibeSpeed.getKnots()/intoTackSpeed.getKnots());
    }

    @Override
    public Pair<Double, SpeedWithBearingWithConfidence<Void>> getManeuverLikelihoodAndTwsTwa(BoatClass boatClass, Speed speedAtManeuverStart, double courseChangeDeg,
            ManeuverType maneuverType) {
        assert maneuverType == ManeuverType.TACK || maneuverType == ManeuverType.JIBE;
        SpeedWithBearingWithConfidence<Void> closestTwsTwa = getClosestTwaTws(maneuverType, speedAtManeuverStart, courseChangeDeg, boatClass);
        final Pair<Double, SpeedWithBearingWithConfidence<Void>> result;
        if (closestTwsTwa == null) {
            result = new Pair<>(0.0, null);
        } else {
            double minDiffDeg = Math.abs(Math.abs(Math.abs(closestTwsTwa.getObject().getBearing().getDegrees() * 2)
                    - Math.abs(courseChangeDeg)));
            result = new Pair<>(1. / (1. + (minDiffDeg / 10.) * (minDiffDeg / 10.)), closestTwsTwa);
        }
        return result;
    }

    private SpeedWithBearingWithConfidence<Void> getClosestTwaTws(ManeuverType type, Speed speedAtManeuverStart, double courseChangeDeg, BoatClass boatClass) {
        assert type == ManeuverType.TACK || type == ManeuverType.JIBE;
        double minDiff = Double.MAX_VALUE;
        SpeedWithBearingWithConfidence<Void> closestTwsTwa = null;
        for (SpeedWithBearingWithConfidence<Void> trueWindSpeedAndAngle : getAverageTrueWindSpeedAndAngleCandidates(
                boatClass, speedAtManeuverStart,
                type == ManeuverType.TACK ? LegType.UPWIND : LegType.DOWNWIND,
                type == ManeuverType.TACK ? courseChangeDeg >= 0 ? Tack.PORT : Tack.STARBOARD
                                          : courseChangeDeg >= 0 ? Tack.STARBOARD : Tack.PORT)) {
            double diff = Math.abs(trueWindSpeedAndAngle.getObject().getBearing().getDegrees()*2)-Math.abs(courseChangeDeg);
            if (diff < minDiff) {
                minDiff = diff;
                closestTwsTwa = trueWindSpeedAndAngle;
            }
        }
        return closestTwsTwa;
    }
}
