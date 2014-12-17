package com.sap.sailing.polars.regression;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.polars.mining.IncrementalRegressionProcessor;

/**
 * Supplies incremental arithmetic mean for confidence and speed.
 * 
 * {@link #estimateSpeed(double, double)} returns the average of the speed
 * 
 * Note that this class should only be used for a small wind interval on a polar sheet. The
 * {@link IncrementalRegressionProcessor} is one example for that. It has one instance of the {@link BoatSpeedEstimator}
 * for each wind boatclass, speed level and rounded angle combination.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class BoatSpeedEstimator implements Serializable {

    private static final long serialVersionUID = -254184914347332658L;

    private double speedSum = 0;

    private double confidenceSum = 0;
    
    private int dataCount = 0;
    
    private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * 
     * @param windSpeed
     * @param angleToTheWind
     * @return
     * @throws NotEnoughDataHasBeenAddedException
     */
    public double estimateSpeed(double windSpeed, double angleToTheWind) throws NotEnoughDataHasBeenAddedException {
        lock.readLock().lock();
        if (dataCount < 1) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        double result;
        try {
                result = speedSum / dataCount;
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    public void addData(double boatSpeed, double confidence) {
        lock.writeLock().lock();
        try {
            speedSum = speedSum + boatSpeed;
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
    
    private void readObject(ObjectInputStream ois) {
        lock = new ReentrantReadWriteLock(true);
    }

}
