package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.regression.BoatSpeedEstimator;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class IncrementalRegressionProcessor implements Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>,Void> {
    
    private static final Logger logger = Logger.getLogger(IncrementalRegressionProcessor.class.getName());

    private final Map<GroupKey, BoatSpeedEstimator> boatSpeedEstimators = new HashMap<GroupKey, BoatSpeedEstimator>();

    private final ClusterGroup<Speed> speedClusterGroup;
    
    private final Set<BoatClass> availableBoatClasses = new HashSet<>();

    public IncrementalRegressionProcessor(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
    }

    @Override
    public void processElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GroupKey key = element.getKey();
        BoatSpeedEstimator boatSpeedEstimator;
        synchronized (boatSpeedEstimators) {
            if (!boatSpeedEstimators.containsKey(key)) {
                boatSpeedEstimators.put(key, new BoatSpeedEstimator());
                availableBoatClasses.add(element.getDataEntry().getBoatClass());
            }

            boatSpeedEstimator = boatSpeedEstimators.get(key);
        }
        GPSFixMovingWithPolarContext fix = element.getDataEntry();

        boatSpeedEstimator.addData(fix.getWindSpeed().getKnots(), fix.getAngleToTheWind().getDegrees(), fix
                .getBoatSpeed().getKnots());
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

    public Pair<SpeedWithConfidence<Integer>, Integer> estimateBoatSpeed(final BoatClass boatClass, final Speed windSpeed,
            final Bearing angleToTheWind)
            throws NotEnoughDataHasBeenAddedException {
        int dataCount = 0;
        PolarClusterKey key = new PolarClusterKey() {
            @Override
            public RoundedAngleToTheWind getRoundedAngleToTheWind() {
                return new RoundedAngleToTheWind(angleToTheWind);
            }

            @Override
            public BoatClass getBoatClass() {
                return boatClass;
            }

            @Override
            public Cluster<Speed> getWindSpeedCluster() {
                return speedClusterGroup.getClusterFor(windSpeed);
            }
        };
        GroupKey compoundKey;
        try {
            compoundKey = GroupKeyFactory.createCompoundKeyFor(key, PolarDataDimensionCollectionFactory
                    .getClusterKeyDimensions().iterator());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        BoatSpeedEstimator boatSpeedEstimator = boatSpeedEstimators.get(compoundKey);
        if (boatSpeedEstimator == null) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        KnotSpeedImpl speedWithoutConfidence = new KnotSpeedImpl(boatSpeedEstimator.estimateSpeed(windSpeed.getKnots(),
                angleToTheWind.getDegrees()));
        dataCount = boatSpeedEstimator.getDataCount();
        return new Pair<SpeedWithConfidence<Integer>, Integer>(new SpeedWithConfidenceImpl<Integer>(
                speedWithoutConfidence, /* FIXME */1.0, /* FIXME */0), dataCount);
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
        return null;
    }

}
