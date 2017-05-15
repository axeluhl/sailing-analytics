package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
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
    private final MeterDistance computedRideHeight;
    private final MeterDistance rideHeightPortHull;
    private final MeterDistance rideHeightStarboardHull;
    private final double pitch;
    private final double heel;
    private final boolean computedIsFoiling;

    public BravoFixImpl(DoubleVectorFix fix) {
        this.fix = fix;

        pitch = fix.get(BravoSensorDataMetadata.INSTANCE.pitchColumn);
        heel = fix.get(BravoSensorDataMetadata.INSTANCE.heelColumn);
        double rideHeightPortHullasDouble = fix.get(BravoSensorDataMetadata.INSTANCE.rideHeightPortHullColumn);
        double rideHeightStarboardHullasDouble = fix
                .get(BravoSensorDataMetadata.INSTANCE.rideHeightStarboardHullColumn);

        rideHeightPortHull = new MeterDistance(rideHeightPortHullasDouble);
        rideHeightStarboardHull = new MeterDistance(rideHeightStarboardHullasDouble);

        computedRideHeight = new MeterDistance(Math.min(rideHeightPortHullasDouble, rideHeightStarboardHullasDouble));
        computedIsFoiling = computedRideHeight.compareTo(MIN_FOILING_HEIGHT_THRESHOLD) >= 0;
    }

    @Override
    public double get(String valueName) {
        int index = BravoSensorDataMetadata.INSTANCE.getTrackColumnIndex(valueName);
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
        return computedRideHeight;
    }

    @Override
    public Distance getRideHeightPortHull() {
        return rideHeightPortHull;
    }

    @Override
    public Distance getRideHeightStarboardHull() {
        return rideHeightStarboardHull;
    }

    @Override
    public boolean isFoiling() {
        return computedIsFoiling;
    }

    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public double getHeel() {
        return heel;
    }
}
