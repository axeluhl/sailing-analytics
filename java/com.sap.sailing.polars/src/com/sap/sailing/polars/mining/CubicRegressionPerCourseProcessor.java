package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class CubicRegressionPerCourseProcessor implements Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, Void> {
    
    private static final Logger logger = Logger.getLogger(CubicRegressionPerCourseProcessor.class.getName());
    
    private final Map<GroupKey, AngleAndSpeedRegression> regressions = new HashMap<>();

    @Override
    public void processElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GroupKey key = element.getKey();
        AngleAndSpeedRegression regression;
        synchronized (regressions) {
            regression = regressions.get(key);
            if (regression == null) {
                regression = new AngleAndSpeedRegression();
                regressions.put(key, regression);
            }
        }
        GPSFixMovingWithPolarContext fix = element.getDataEntry();
        regression.addData(fix.getWindSpeed(), fix.getAngleToTheWind(), fix.getBoatSpeed());
    }
    
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedAndCourseOverGround(BoatClass boatClass, Speed windSpeed, LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        GroupKey key = createGroupKey(boatClass, legType, tack);
        SpeedWithBearingWithConfidence<Void> estimatedSpeedAndAngle = null;
        if (regressions.containsKey(key)) {
            estimatedSpeedAndAngle = regressions.get(key).estimateSpeedAndAngle(windSpeed);
        } else {
            throw new NotEnoughDataHasBeenAddedException("Not enough data has been added to Per Course Regressions");
        }
        return estimatedSpeedAndAngle;
    }
    
    public Set<SpeedWithBearingWithConfidence<Void>> estimateTrueWindSpeedAndAngleCandidates(BoatClass boatClass,
            Speed speedOverGround, LegType legType, Tack tack) {
        GroupKey key = createGroupKey(boatClass, legType, tack);
        Set<SpeedWithBearingWithConfidence<Void>> result = new HashSet<>();
        if (regressions.containsKey(key)) {
            try {
                result = regressions.get(key).estimateTrueWindSpeedAndAngleCandidates(speedOverGround);
            } catch (NotEnoughDataHasBeenAddedException e) {
                // Return empty result
            }
        } else {
            // Return empty result;
        }
        return result;
    }
    
    private GroupKey createGroupKey(final BoatClass boatClass, final LegType legType,
            final Tack tack) {
        TackAndLegTypePolarClusterKey key = new TackAndLegTypePolarClusterKey() {

            @Override
            public BoatClass getBoatClass() {
                return boatClass;
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
                    .getCubicRegressionPerCourseClusterKeyDimensions().iterator());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return compoundKey;
    }

    @Override
    public void onFailure(Throwable failure) {
        logger.severe("Polar Data Mining Pipe failed.");
        throw new RuntimeException("Polar Data Miner failed.", failure);
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

}
