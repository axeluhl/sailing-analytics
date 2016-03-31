package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

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

}
