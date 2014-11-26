package com.sap.sailing.polars.regression;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.polars.mining.IncrementalRegressionProcessor;
import com.sap.sailing.polars.regression.impl.IncrementalLeastSquaresProcessor;

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

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private double confidenceSum = 0;
    
    private int dataCount = 0;

    /**
     * 
     * @param windSpeed
     * @param angleToTheWind
     * @param useLinearRegression if false mean of wind interval is used, otherwise lin. regression
     * @return
     * @throws NotEnoughDataHasBeenAddedException
     */
    public double estimateSpeed(double windSpeed, double angleToTheWind, boolean useLinearRegression) throws NotEnoughDataHasBeenAddedException {
        lock.readLock().lock();
        try {
            double angleEstimated = angleRegression.getEstimatedY(angleToTheWind);
            double windSpeedEstimated = windSpeedRegression.getEstimatedY(windSpeed);
            return (angleEstimated + windSpeedEstimated) / 2.0;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addData(double windSpeed, double angleToTheWind, double boatSpeed, double confidence) {
        lock.writeLock().lock();
        try {
            angleRegression.addMeasuredPoint(angleToTheWind, boatSpeed);
            windSpeedRegression.addMeasuredPoint(windSpeed, boatSpeed);
            dataCount++;
            confidenceSum  = confidenceSum + confidence;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getDataCount() {
        return dataCount;
    }

    public double getConfidence() {
        return confidenceSum / dataCount;
    }

}
