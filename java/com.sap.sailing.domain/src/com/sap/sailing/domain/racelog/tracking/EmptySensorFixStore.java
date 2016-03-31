package com.sap.sailing.domain.racelog.tracking;

import java.util.function.Consumer;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;

public enum EmptySensorFixStore implements SensorFixStore {
    INSTANCE;

    @Override
    public void addListener(GPSFixReceivedListener listener, DeviceIdentifier device) {
    }

    @Override
    public void removeListener(GPSFixReceivedListener listener) {
    }

    @Override
    public TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) {
        return null;
    }

    @Override
    public long getNumberOfFixes(DeviceIdentifier device) {
        return 0;
    }

    @Override
    public <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier,
            TimePoint start, TimePoint end, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
            TransformationException {
    }

    @Override
    public <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix) {
    }

}
