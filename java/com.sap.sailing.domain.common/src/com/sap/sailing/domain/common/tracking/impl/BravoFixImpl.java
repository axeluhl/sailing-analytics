package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
    public Distance getRideHeight() {
        return Util.min(getRideHeightPortHull(), getRideHeightStarboardHull());
    }
    
    @Override
    public Distance getRideHeightPortHull() {
        return new MeterDistance(fix.get(BravoSensorDataMetadata.INSTANCE.rideHeightPortHullColumn));
    }
    
    @Override
    public Distance getRideHeightStarboardHull() {
        return new MeterDistance(fix.get(BravoSensorDataMetadata.INSTANCE.rideHeightStarboardHullColumn));
    }
    
    @Override
    public boolean isFoiling() {
        return getRideHeight().compareTo(MIN_FOILING_HEIGHT_THRESHOLD) >= 0;
    }

    @Override
    public double getPitch() {
        return fix.get(BravoSensorDataMetadata.INSTANCE.pitchColumn);
    }
    
    @Override
    public double getYaw() {
        return fix.get(BravoSensorDataMetadata.INSTANCE.yawColumn);
    }
    
    @Override
    public double getRoll() {
        return fix.get(BravoSensorDataMetadata.INSTANCE.rollColumn);
    }
}
