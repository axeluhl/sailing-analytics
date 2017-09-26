package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

/**
 * Implementation of {@link BravoFix} that wraps a {@link DoubleVectorFix} which holds the actual sensor data. The
 * mapping metadata is stored in the {@link BravoSensorDataMetadata} enum.
 */
public class BravoFixImpl implements BravoFix {
    private static final long serialVersionUID = 2033254212013221160L;
    private final DoubleVectorFix fix;

    public BravoFixImpl(DoubleVectorFix fix) {
        this.fix = fix;
    }

    @Override
    public double get(String valueName) {
        BravoSensorDataMetadata colDefEnum = BravoSensorDataMetadata.byColumnName(valueName);
        if (colDefEnum == null) {
            throw new IllegalArgumentException("Unknown value \"" + valueName + "\" for " + getClass().getSimpleName());
        }
        int index = colDefEnum.getColumnIndex();
        return fix.get(index);
    }

    @Override
    public TimePoint getTimePoint() {
        return fix.getTimePoint();
    }

    @Override
    public Distance getRideHeight() {
        double rideHeightPortHullasDouble = fix.get(BravoSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex());
        double rideHeightStarboardHullasDouble = fix
                .get(BravoSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex());
        return new MeterDistance(Math.min(rideHeightPortHullasDouble, rideHeightStarboardHullasDouble));
    }

    @Override
    public Distance getRideHeightPortHull() {
        double rideHeightPortHullasDouble = fix.get(BravoSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex());
        return new MeterDistance(rideHeightPortHullasDouble);
    }

    @Override
    public Distance getRideHeightStarboardHull() {
        double rideHeightStarboardHullasDouble = fix
                .get(BravoSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex());
        return new MeterDistance(rideHeightStarboardHullasDouble);
    }

    @Override
    public boolean isFoiling() {
        return isFoiling(MIN_FOILING_HEIGHT_THRESHOLD);
    }
    
    @Override
    public boolean isFoiling(Distance minimumRideHeight) {
        return getRideHeight().compareTo(minimumRideHeight) >= 0;
    }

    @Override
    public Bearing getPitch() {
        return new DegreeBearingImpl(fix.get(BravoSensorDataMetadata.PITCH.getColumnIndex()));
    }

    @Override
    public Bearing getHeel() {
        return new DegreeBearingImpl(fix.get(BravoSensorDataMetadata.HEEL.getColumnIndex()));
    }
}
