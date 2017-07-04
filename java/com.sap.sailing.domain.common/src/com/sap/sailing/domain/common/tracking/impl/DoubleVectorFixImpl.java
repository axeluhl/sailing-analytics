package com.sap.sailing.domain.common.tracking.impl;

import java.util.Arrays;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

/**
 * Implementation of {@link DoubleVectorFix}.
 */
public class DoubleVectorFixImpl implements DoubleVectorFix {

    private static final long serialVersionUID = -425848879310273855L;
    
    private final double[] fixData;
    private final TimePoint timePoint;

    public DoubleVectorFixImpl(TimePoint timePoint, double[] fixData) {
        this.timePoint = timePoint;
        this.fixData = fixData;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }
    
    @Override
    public double get(int index) {
        return fixData[index];
    }

    @Override
    public double[] get() {
        // TODO defensive copy?
        return fixData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fixData);
        result = prime * result + ((timePoint == null) ? 0 : timePoint.hashCode());
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
        DoubleVectorFixImpl other = (DoubleVectorFixImpl) obj;
        if (!Arrays.equals(fixData, other.fixData))
            return false;
        if (timePoint == null) {
            if (other.timePoint != null)
                return false;
        } else if (!timePoint.equals(other.timePoint))
            return false;
        return true;
    }
}
