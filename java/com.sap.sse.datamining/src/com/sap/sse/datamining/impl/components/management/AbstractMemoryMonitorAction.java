package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.components.management.MemoryMonitorAction;

/**
 * Triggers an abort when the free memory ratio is below a given threshold and the absolute free memory
 * is below a certain threshold, too. This should give good results for different sizes of heaps; in particular,
 * for an "ARCHIVE" server workload with heap sizes of several hundred GBs, a fixed ratio may not be adequate alone
 * because 10% of 500GB = 50GB may still be lots for a data mining query.
 */
public abstract class AbstractMemoryMonitorAction implements MemoryMonitorAction {

    private final double thresholdFreeMemoryRatio;
    private final long minFreeMemoryInBytes;

    public AbstractMemoryMonitorAction(double freeMemoryRatioThreshold, long minFreeMemoryInBytes) {
        this.thresholdFreeMemoryRatio = freeMemoryRatioThreshold;
        this.minFreeMemoryInBytes = minFreeMemoryInBytes;
    }

    @Override
    public double getThreshold() {
        return thresholdFreeMemoryRatio;
    }

    @Override
    public boolean checkMemoryAndPerformAction(double freeMemoryRatio, long freeMemoryInBytes) {
        if (freeMemoryRatio < thresholdFreeMemoryRatio && freeMemoryInBytes < minFreeMemoryInBytes) {
            performAction();
            return true;
        }
        return false;
    }

    public abstract void performAction();

    @Override
    public int compareTo(MemoryMonitorAction other) {
        // An action is more important the lower the threshold is.
        return Double.compare(this.getThreshold(), other.getThreshold());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[threshold: " + getThreshold() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(thresholdFreeMemoryRatio);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMemoryMonitorAction other = (AbstractMemoryMonitorAction) obj;
        if (Double.doubleToLongBits(thresholdFreeMemoryRatio) != Double.doubleToLongBits(other.thresholdFreeMemoryRatio))
            return false;
        return true;
    }
    
}
