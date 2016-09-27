package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.components.management.MemoryMonitorAction;

public abstract class AbstractMemoryMonitorAction implements MemoryMonitorAction {

    private final double threshold;

    public AbstractMemoryMonitorAction(double freeMemoryRatioThreshold) {
        this.threshold = freeMemoryRatioThreshold;
    }

    @Override
    public double getThreshold() {
        return threshold;
    }

    @Override
    public boolean checkMemoryAndPerformAction(double freeMemoryRatio) {
        if (freeMemoryRatio < threshold) {
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
        temp = Double.doubleToLongBits(threshold);
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
        if (Double.doubleToLongBits(threshold) != Double.doubleToLongBits(other.threshold))
            return false;
        return true;
    }
    
}
