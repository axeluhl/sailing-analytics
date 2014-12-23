package com.sap.sailing.polars.regression;

import com.sap.sailing.polars.mining.IncrementalRegressionProcessor;
import com.sap.sailing.polars.regression.impl.IncrementalLeastSquaresProcessor;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * Combines two linear regression processors. One with the angle to the wind as the x axis and the other with the wind
 * speed on the x axis. Both y axis represent the boat speed.
 * 
 * {@link #estimateSpeed(double, double)} returns the average of the estimation of both regressions.
 * 
 * Note that this class should only be used for a small interval on a polar sheet. The
 * {@link IncrementalRegressionProcessor} is one example for that. It has one instance of the {@link BoatSpeedEstimator}
 * for each wind speed level and rounded angle combination.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class BoatSpeedEstimator {

    private IncrementalLinearRegressionProcessor angleRegression = new IncrementalLeastSquaresProcessor();

    private IncrementalLinearRegressionProcessor windSpeedRegression = new IncrementalLeastSquaresProcessor();

    private NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock(getClass().getName(), true);

    private double confidenceSum = 0;
    
    private int dataCount = 0;

    /**
     * @param useLinearRegression if false mean of wind interval is used, otherwise linear regression
     */
    public double estimateSpeed(double windSpeed, double angleToTheWind, boolean useLinearRegression) throws NotEnoughDataHasBeenAddedException {
        LockUtil.lockForRead(lock);
        double result;
        try {
            if (useLinearRegression) {
                double angleEstimated = angleRegression.getEstimatedY(angleToTheWind);
                double windSpeedEstimated = windSpeedRegression.getEstimatedY(windSpeed);

                result = (angleEstimated + windSpeedEstimated) / 2.0;
            } else {
                //Return average for that wind interval
                result = windSpeedRegression.getMeanOfY();
            }
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        return result;
    }

    public void addData(double windSpeed, double angleToTheWind, double boatSpeed, double confidence) {
        LockUtil.lockForWrite(lock);
        try {
            angleRegression.addMeasuredPoint(angleToTheWind, boatSpeed);
            windSpeedRegression.addMeasuredPoint(windSpeed, boatSpeed);
            dataCount++;
            confidenceSum  = confidenceSum + confidence;
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    public int getDataCount() {
        return dataCount;
    }

    public double getConfidence() {
        return confidenceSum / dataCount;
    }

}
