package com.sap.sailing.domain.racelog.tracking;

import java.util.function.Consumer;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;


public interface SensorFixStore {
    
    <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier, TimePoint start, TimePoint end, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
    TransformationException;

    <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix);

    /**
     * Listeners are notified, whenever a {@link GPSFix} submitted by the {@code device}
     * is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)} method.
     */
    void addListener(FixReceivedListener listener, DeviceIdentifier device);

    /**
     * Remove the registrations of the listener for all devices.
     */
    void removeListener(FixReceivedListener listener);
    
    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException;
}
