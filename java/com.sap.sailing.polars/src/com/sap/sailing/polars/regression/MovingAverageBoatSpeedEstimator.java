package com.sap.sailing.polars.regression;

import java.io.ObjectInputStream;
import java.io.Serializable;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.mining.MovingAverageProcessorImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Supplies incremental arithmetic mean for confidence and speed.
 * <p>
 * 
 * {@link #estimateSpeed(double, double)} returns the average of the speed.
 * <p>
 * 
 * Note that this class should only be used for a small wind interval on a polar sheet. The
 * {@link MovingAverageProcessorImpl} is one example for that. It has one instance of the {@link MovingAverageBoatSpeedEstimator}
 * for each wind boatclass, speed level and rounded angle combination.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class MovingAverageBoatSpeedEstimator implements Serializable {

    private static final long serialVersionUID = -254184914347332658L;

    private double speedSumInKnots = 0;

    private transient NamedReentrantReadWriteLock lock = createLock();
    
    private double confidenceSum = 0;
    
    private int dataCount = 0;

    public Speed estimateSpeed() throws NotEnoughDataHasBeenAddedException {
        LockUtil.lockForRead(lock);
        if (dataCount < 1) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        try {
            return new KnotSpeedImpl(speedSumInKnots / dataCount);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    public void addData(Speed boatSpeed, double confidence) {
        LockUtil.lockForWrite(lock);
        try {
            speedSumInKnots = speedSumInKnots + boatSpeed.getKnots();
            dataCount++;
            confidenceSum  = confidenceSum + confidence;
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    public int getDataCount() {
        LockUtil.lockForRead(lock);
        try {
            return dataCount;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    public double getConfidence() {
        LockUtil.lockForRead(lock);
        try {
            return confidenceSum / dataCount;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }
    
    private void readObject(ObjectInputStream ois) {
        lock = createLock();
    }
    
    private NamedReentrantReadWriteLock createLock() {
        return new NamedReentrantReadWriteLock(getClass().getName(), true);
    }

}
