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
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.polars.regression.MovingAverageBoatSpeedEstimator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class MovingAverageProcessorImpl implements MovingAverageProcessor {

    private static final Logger logger = Logger.getLogger(MovingAverageProcessorImpl.class.getName());

    private final Map<GroupKey, MovingAverageBoatSpeedEstimator> boatSpeedEstimators = new HashMap<>();
    
    private final Map<GroupKey, AverageAngleContainer> averageAngleContainers = new HashMap<>();

    private final ClusterGroup<Speed> speedClusterGroup;

    private final Set<BoatClass> availableBoatClasses = new HashSet<>();



    public MovingAverageProcessorImpl(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
    }

    @Override
    public void processElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GPSFixMovingWithPolarContext fix = element.getDataEntry();
        if (fix.getLegType() == LegType.UPWIND || fix.getLegType() == LegType.DOWNWIND) {
            GroupKey key = element.getKey();
            MovingAverageBoatSpeedEstimator boatSpeedEstimator;
            synchronized (boatSpeedEstimators) {
                boatSpeedEstimator = boatSpeedEstimators.get(key);
                if (boatSpeedEstimator == null) {
                    boatSpeedEstimator = new MovingAverageBoatSpeedEstimator();
                    boatSpeedEstimators.put(key, boatSpeedEstimator);
                }
            }
            AverageAngleContainer averageAngleContainer;
            synchronized (averageAngleContainers) {
                averageAngleContainer = averageAngleContainers.get(key);
                if (averageAngleContainer == null) {
                    averageAngleContainer = new AverageAngleContainer();
                    averageAngleContainers.put(key, averageAngleContainer);
                }
            }
            BearingWithConfidence<Integer> angleToTheWind = fix.getAngleToTheWind();
            WindWithConfidence<Pair<Position, TimePoint>> windSpeed = fix.getWind();
            SpeedWithBearingWithConfidence<TimePoint> boatSpeedWithConfidence = fix.getBoatSpeed();
            availableBoatClasses.add(element.getDataEntry().getBoatClass());
            WindWithConfidence<Pair<Position, TimePoint>> windWithConfidenceForSpeed = windSpeed;
            double confidenceForWindSpeed = windWithConfidenceForSpeed.getConfidence();
            double confidenceForWindBearing = angleToTheWind.getConfidence();
            double confidenceForBoatSpeed = boatSpeedWithConfidence.getConfidence();
            double averagedConfidence = (confidenceForBoatSpeed + confidenceForWindBearing + confidenceForWindSpeed) / 3;
            averageAngleContainer.addFix(angleToTheWind.getObject().getDegrees());
            boatSpeedEstimator.addData(boatSpeedWithConfidence.getObject(), averagedConfidence);
        }
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
    
    private SpeedWithConfidenceAndDataCount estimateBoatSpeed(GroupKey key) throws NotEnoughDataHasBeenAddedException {
        MovingAverageBoatSpeedEstimator boatSpeedEstimator = boatSpeedEstimators.get(key);
        if (boatSpeedEstimator == null) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        Speed speedWithoutConfidence = boatSpeedEstimator.estimateSpeed();
        final int dataCount = boatSpeedEstimator.getDataCount();
        double confidence = boatSpeedEstimator.getConfidence();
        return new SpeedWithConfidenceAndDataCount(new SpeedWithConfidenceImpl<Void>(speedWithoutConfidence, confidence, null), dataCount);
    }
    
    private Bearing estimateAngle(GroupKey key) throws NotEnoughDataHasBeenAddedException {
        AverageAngleContainer averageAngleContainer = averageAngleContainers.get(key);
        if (averageAngleContainer == null) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        return new DegreeBearingImpl(averageAngleContainer.getAverageAngleDeg());
    }

    @Override
    public void onFailure(Throwable failure) {
        logger.severe("Polar Data Mining Pipe failed.");
        throw new RuntimeException("Polar Data Miner failed.", failure);
    }

    public Set<BoatClass> getAvailableBoatClasses() {
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
        SpeedWithBearingWithConfidence<Void> result = estimateSpeedAndAngle(boatClass, windSpeed, legType, tack);
        return result;
    }

    private SpeedWithBearingWithConfidence<Void> estimateSpeedAndAngle(BoatClass boatClass, Speed windSpeed, LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        GroupKey key = createGroupKey(boatClass, windSpeed, legType, tack);
        Bearing angleDiffTrueWindToBoat = estimateAngle(key);
        SpeedWithConfidence<Void> estimatedSpeed = estimateBoatSpeed(key).getSpeedWithConfidence();
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(estimatedSpeed.getObject().getKnots(), angleDiffTrueWindToBoat);
        return new SpeedWithBearingWithConfidenceImpl<Void>(speedWithBearing, estimatedSpeed.getConfidence(), null);
    }
    
    private GroupKey createGroupKey(final BoatClass boatClass, final Speed windSpeed, final LegType legType,
            final Tack tack) {
        MovingAveragePolarClusterKey key = new MovingAveragePolarClusterKey() {

            @Override
            public BoatClass getBoatClass() {
                return boatClass;
            }

            @Override
            public Cluster<Speed> getWindSpeedCluster() {
                return speedClusterGroup.getClusterFor(windSpeed);
            }

            @Override
            public Tack getTack() {
                return tack;
            }

            @Override
            public LegType getLegType() {
                return legType;
            }
        };
        GroupKey compoundKey;
        try {
            compoundKey = GroupKeyFactory.createNestingCompoundKeyFor(key, PolarDataDimensionCollectionFactory
                    .getMovingAverageClusterKeyDimensions().iterator());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return compoundKey;
    }

}
