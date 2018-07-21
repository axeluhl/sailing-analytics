package com.sap.sailing.domain.common.tracking.impl;

import java.util.Arrays;
import java.util.BitSet;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

/**
 * Implementation of {@link DoubleVectorFix}. In order to save some space and reduce the number of object headers,
 * instead of storing {@link Double} objects the structure internally uses an array of {@code double} values and
 * additionally remembers in a {@link BitSet} which components are {@code null}. The {@link #get()} and {@link #get(int)}
 * methods then translate back to a {@link Double} representation accordingly.
 */
public class DoubleVectorFixImpl implements DoubleVectorFix {

    private static final long serialVersionUID = -425848879310273855L;
    
    private final double[] fixData;
    private final TimePoint timePoint;
    
    /**
     * The constructor accepts a {@code Double[]} for the data components, allowing for {@code null}
     * values to be used. To save space, internally a {@code double[]} is used to represent the data.
     * In order to encode the {@code null} values, this bit set stores which array components had
     * valid, non-{@code null} data.
     */
    private final BitSet validComponents;

    public DoubleVectorFixImpl(TimePoint timePoint, Double[] fixData) {
        // We determine the last valid component, and omit everything after to safe space
        int usedSize = getLastUsedDouble(fixData);
        this.timePoint = timePoint;
        this.fixData = new double[usedSize];
        this.validComponents = new BitSet(usedSize);
        for (int i = 0; i < usedSize; i++) {
            if (fixData[i] != null) {
                this.validComponents.set(i);
                this.fixData[i] = fixData[i];
            }
        }
    }

    private int getLastUsedDouble(Double[] fixData) {
        for (int i = fixData.length - 1; i >= 0; i--) {
            if (fixData[i] != null) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }
    
    @Override
    public Double get(int index) {
        return index<fixData.length && validComponents.get(index) ? fixData[index] : null;
    }

    @Override
    public Double[] get() {
        final Double[] result = new Double[fixData.length];
        for (int i=0; i<fixData.length; i++) {
            result[i] = validComponents.get(i) ? fixData[i] : null;
        }
        return result;
    }

    @Override
    public boolean hasValidData() {
        return !validComponents.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fixData);
        result = prime * result + ((timePoint == null) ? 0 : timePoint.hashCode());
        result = prime * result + ((validComponents == null) ? 0 : validComponents.hashCode());
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
        if (validComponents == null) {
            if (other.validComponents != null)
                return false;
        } else if (!validComponents.equals(other.validComponents))
            return false;
        return true;
    }
}
