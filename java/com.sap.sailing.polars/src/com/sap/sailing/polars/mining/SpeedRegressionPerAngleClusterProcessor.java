package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class SpeedRegressionPerAngleClusterProcessor implements Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, Void>{
    
private static final Logger logger = Logger.getLogger(CubicRegressionPerCourseProcessor.class.getName());
    
    private final Map<GroupKey, IncrementalLeastSquares> regressions = new HashMap<>();

    private final ClusterGroup<Bearing> angleClusterGroup;
    
    public SpeedRegressionPerAngleClusterProcessor(ClusterGroup<Bearing> angleClusterGroup) {
        this.angleClusterGroup = angleClusterGroup;
    }
    
    @Override
        public boolean canProcessElements() {
            // TODO Auto-generated method stub
            return true;
        }

    @Override
    public void processElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GroupKey key = element.getKey();
        IncrementalLeastSquares regression;
        synchronized (regressions) {
            regression = regressions.get(key);
            if (regression == null) {
                regression = new IncrementalAnyOrderLeastSquaresImpl(3, false);
                regressions.put(key, regression);
            }
        }
        GPSFixMovingWithPolarContext fix = element.getDataEntry();
        regression.addData(fix.getWind().getObject().getKnots(), fix.getBoatSpeed().getObject().getKnots());
    }
    
    /**
     * There are angle clusters (size defined in the data mining pipeline construction), which each have their own 
     * regression for boatspeed over windspeed. 
     * We don't know the thresholds or centers of the angle clusters here, so we roughly interpolate by taking 10 
     * values from angle-5 deg to angle+5 deg and average the speeds.
     */
    public SpeedWithConfidence<Void> estimateBoatSpeed(BoatClass boatClass, Speed windSpeed, Bearing trueWindAngle) throws NotEnoughDataHasBeenAddedException {
        double speedSum = 0;
        double numberOfSpeeds = 0;
        for (int i = -5; i <= 5; i++) {
            GroupKey key = createGroupKey(boatClass, new DegreeBearingImpl(trueWindAngle.getDegrees() + i));
            if (regressions.containsKey(key)) {
                speedSum += regressions.get(key).getOrCreatePolynomialFunction().value(windSpeed.getKnots());
                numberOfSpeeds++;
            } 
        }
        if (numberOfSpeeds < 1) {
            throw new NotEnoughDataHasBeenAddedException("Not enough data has been added to Per Course Regressions");
        }
        Speed speed = new KnotSpeedImpl(speedSum / numberOfSpeeds);
        return new SpeedWithConfidenceImpl<Void>(speed, /*FIXME*/ 0.5, null);
    }
    
    private GroupKey createGroupKey(final BoatClass boatClass, final Bearing angle) {
        AngleClusterPolarClusterKey key = new AngleClusterPolarClusterKey() {

            @Override
            public BoatClass getBoatClass() {
                return boatClass;
            }

            @Override
            public Cluster<Bearing> getAngleCluster() {
                return angleClusterGroup.getClusterFor(angle);
            }

        };
        GroupKey compoundKey;
        try {
            compoundKey = GroupKeyFactory.createNestingCompoundKeyFor(key, PolarDataDimensionCollectionFactory
                    .getSpeedRegressionPerAngleClusterClusterKeyDimensions().iterator());
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
    
    public PolynomialFunction getSpeedRegressionFunction(BoatClass boatClass, double trueWindAngle)
            throws NotEnoughDataHasBeenAddedException {
        GroupKey key = createGroupKey(boatClass, new DegreeBearingImpl(trueWindAngle));
        PolynomialFunction polynomialFunction;
        if (regressions.containsKey(key)) {
            polynomialFunction = regressions.get(key).getOrCreatePolynomialFunction();
        } else {
            throw new NotEnoughDataHasBeenAddedException();
        }
        return polynomialFunction;
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
