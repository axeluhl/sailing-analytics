package com.sap.sailing.polars.regression;

import java.io.ObjectInputStream;
import java.io.Serializable;
import com.sap.sailing.polars.mining.IncrementalRegressionProcessor;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

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

    private transient NamedReentrantReadWriteLock lock = createLock();
    
    private double confidenceSum = 0;
    
    private int dataCount = 0;

    /**
     * @param useLinearRegression if false mean of wind interval is used, otherwise linear regression
     */
    public double estimateSpeed(double windSpeed, double angleToTheWind) throws NotEnoughDataHasBeenAddedException {
        LockUtil.lockForRead(lock);
        if (dataCount < 1) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        double result;
        try {
                result = speedSum / dataCount;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        return result;
    }

    public void addData(double boatSpeed, double confidence) {
        LockUtil.lockForWrite(lock);
        try {
            speedSum = speedSum + boatSpeed;
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
    
    private void readObject(ObjectInputStream ois) {
        lock = createLock();
    }
    
    private NamedReentrantReadWriteLock createLock() {
        return new NamedReentrantReadWriteLock(getClass().getName(), true);
    }

}
