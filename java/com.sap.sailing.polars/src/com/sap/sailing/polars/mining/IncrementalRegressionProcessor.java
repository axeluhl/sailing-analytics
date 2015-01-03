package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.regression.BoatSpeedEstimator;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class IncrementalRegressionProcessor implements Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, Void>,
        PolarSheetAnalyzer {

    private static final Logger logger = Logger.getLogger(IncrementalRegressionProcessor.class.getName());

    private final Map<GroupKey, BoatSpeedEstimator> boatSpeedEstimators = new HashMap<GroupKey, BoatSpeedEstimator>();

    private final ClusterGroup<Speed> speedClusterGroup;

    private final Set<BoatClassMasterdata> availableBoatClasses = new HashSet<>();

    private final Map<Pair<LegType, Tack>, AverageAngleContainer> averageAngleContainers;

    public IncrementalRegressionProcessor(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
        averageAngleContainers = new HashMap<>();
        for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
            for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                averageAngleContainers.put(new Pair<LegType, Tack>(legType, tack),
                        new AverageAngleContainer(speedClusterGroup));
            }
        }
    }

    @Override
    public void processElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GroupKey key = element.getKey();
        BoatSpeedEstimator boatSpeedEstimator;
        synchronized (boatSpeedEstimators) {
            boatSpeedEstimator = boatSpeedEstimators.get(key);
            if (boatSpeedEstimator == null) {
                boatSpeedEstimator = new BoatSpeedEstimator();
                boatSpeedEstimators.put(key, boatSpeedEstimator);
                availableBoatClasses.add(element.getDataEntry().getBoatClassMasterData());
            }
        }
        GPSFixMovingWithPolarContext fix = element.getDataEntry();
        BearingWithConfidence<Integer> angleToTheWind = fix.getAngleToTheWind();
        WindWithConfidence<Pair<Position, TimePoint>> windSpeed = fix.getWindSpeed();
        SpeedWithBearingWithConfidence<TimePoint> boatSpeedWithConfidence = fix.getBoatSpeed();
        // Only add GPS data if speeds and angles are not null, else do nothing!
        if (angleToTheWind != null && windSpeed != null && boatSpeedWithConfidence != null) {
            fillAverageAngleContainer(fix, element, windSpeed);
            WindWithConfidence<Pair<Position, TimePoint>> windWithConfidenceForSpeed = windSpeed;
            double confidenceForWindSpeed = windWithConfidenceForSpeed.getConfidence();
            double confidenceForWindBearing = angleToTheWind.getConfidence();
            double confidenceForBoatSpeed = boatSpeedWithConfidence.getConfidence();
            double averagedConfidence = (confidenceForBoatSpeed + confidenceForWindBearing + confidenceForWindSpeed) / 3;
            boatSpeedEstimator.addData(boatSpeedWithConfidence.getObject(), averagedConfidence);
        }
    }

    private void fillAverageAngleContainer(GPSFixMovingWithPolarContext fix,
            GroupedDataEntry<GPSFixMovingWithPolarContext> element,
            WindWithConfidence<Pair<Position, TimePoint>> windSpeed) {
        final int roundedAngleDeg = fix.getRoundedTrueWindAngle().getAngleDeg();
        final LegType legType;
        final Tack tack;
        if (roundedAngleDeg < -90) {
            legType = LegType.DOWNWIND;
            tack = Tack.PORT;
        } else if (roundedAngleDeg < 0) {
            legType = LegType.UPWIND;
            tack = Tack.PORT;
        } else if (roundedAngleDeg > 90) {
            legType = LegType.DOWNWIND;
            tack = Tack.STARBOARD;
        } else { // roundedAngleDeg > 0 && roundedAngleDeg <= 90
            legType = LegType.UPWIND;
            tack = Tack.STARBOARD;
        }
        averageAngleContainers.get(new Pair<>(legType, tack)).addFix(element.getDataEntry().getBoatClassMasterData(),
                windSpeed.getObject(), roundedAngleDeg);
    }

    @Override
    public void finish() throws InterruptedException {
        // Nothing to do here
    }

    @Override
    public void abort() {
        // TODO Auto-generated method stub
    }

    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

    public static class SpeedWithConfidenceAndDataCount {
        private final SpeedWithConfidence<Void> speedWithConfidence;
        private final int dataCount;
        protected SpeedWithConfidenceAndDataCount(SpeedWithConfidence<Void> speedWithConfidence, int dataCount) {
            super();
            this.speedWithConfidence = speedWithConfidence;
            this.dataCount = dataCount;
        }
        public SpeedWithConfidence<Void> getSpeedWithConfidence() {
            return speedWithConfidence;
        }
        public int getDataCount() {
            return dataCount;
        }
    }
    
    SpeedWithConfidenceAndDataCount estimateBoatSpeed(final BoatClass boatClass, final Speed windSpeed,
            final Bearing trueWindAngle) throws NotEnoughDataHasBeenAddedException {
        PolarClusterKey key = new PolarClusterKey() {
            @Override
            public RoundedTrueWindAngle getRoundedTrueWindAngle() {
                return new RoundedTrueWindAngle(trueWindAngle);
            }

            @Override
            public BoatClassMasterdata getBoatClassMasterData() {
                return BoatClassMasterdata.resolveBoatClass(boatClass.getName());
            }

            @Override
            public Cluster<Speed> getWindSpeedCluster() {
                return speedClusterGroup.getClusterFor(windSpeed);
            }
        };
        GroupKey compoundKey;
        try {
            compoundKey = GroupKeyFactory.createNestingCompoundKeyFor(key, PolarDataDimensionCollectionFactory
                    .getClusterKeyDimensions().iterator());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        BoatSpeedEstimator boatSpeedEstimator = boatSpeedEstimators.get(compoundKey);
        if (boatSpeedEstimator == null) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        Speed speedWithoutConfidence = boatSpeedEstimator.estimateSpeed(windSpeed.getKnots(), trueWindAngle.getDegrees());
        final int dataCount = boatSpeedEstimator.getDataCount();
        double confidence = boatSpeedEstimator.getConfidence();
        return new SpeedWithConfidenceAndDataCount(new SpeedWithConfidenceImpl<Void>(speedWithoutConfidence, confidence, null), dataCount);
    }

    SpeedWithBearingWithConfidence<Void> estimateTrueWindSpeedAndAngle(BoatClass boatClass,
            Speed speedOverGround, LegType legType, Tack tack) {
        AverageAngleContainer averageAngleContainer = averageAngleContainers.get(new Pair<>(legType, tack));
        /* TODO use */ averageAngleContainer.toString(); /* to calculate the most likely true wind speed/angle
         * assuming the boat was going at a reasonable VMG in the legType on its tack */
        return null;
    }

    @Override
    public void onFailure(Throwable failure) {
        logger.severe("Polar Data Mining Pipe failed.");
        throw new RuntimeException("Polar Data Miner failed.", failure);
    }

    public Set<BoatClassMasterdata> getAvailableBoatClasses() {
        return availableBoatClasses;
    }

    @Override
    public Class<GroupedDataEntry<GPSFixMovingWithPolarContext>> getInputType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Void> getResultType() {
        // No result type here, since this is a special case of a processor. It's the end of the pipe so to say.
        return null;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedAndCourseOverGround(BoatClass boatClass,
            Speed windSpeed, LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        Double averageAngle = averageAngleContainers.get(new Pair<>(legType, tack)).getAverageAngleDeg(
                BoatClassMasterdata.resolveBoatClass(boatClass.getName()), windSpeed);
        return estimateSpeedForAverageAngle(boatClass, windSpeed, averageAngle);
    }

    private SpeedWithBearingWithConfidence<Void> estimateSpeedForAverageAngle(BoatClass boatClass, Speed windSpeed,
            Double averageAngle) throws NotEnoughDataHasBeenAddedException {
        if (averageAngle == null) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        DegreeBearingImpl angleToTheWind = new DegreeBearingImpl(averageAngle);
        SpeedWithConfidence<Void> estimatedSpeed = estimateBoatSpeed(boatClass, windSpeed, angleToTheWind).getSpeedWithConfidence();
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(estimatedSpeed.getObject().getKnots(),
                angleToTheWind);
        return new SpeedWithBearingWithConfidenceImpl<Void>(speedWithBearing, estimatedSpeed.getConfidence(), null);
    }

}
