package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

/**
 * Implementation of {@link BravoFix} that wraps a {@link DoubleVectorFix} which holds the actual sensor data.
 */
public class BravoFixImpl implements BravoFix {
    private static final long serialVersionUID = 2033254212013221160L;
    
    private final DoubleVectorFix fix;

    public BravoFixImpl(DoubleVectorFix fix) {
        this.fix = fix;
    }

    @Override
    public double get(String valueName) {
        int index = BravoSensorDataMetadata.INSTANCE.getColumnIndex(valueName);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown value \"" + valueName + "\" for " + getClass().getSimpleName());
        }
        return fix.get(index);
    }

    @Override
    public TimePoint getTimePoint() {
        return fix.getTimePoint();
    }

    @Override
    public double getRideHeight() {
        return fix.get(BravoSensorDataMetadata.INSTANCE.rideHeightColumn);
    }

}
