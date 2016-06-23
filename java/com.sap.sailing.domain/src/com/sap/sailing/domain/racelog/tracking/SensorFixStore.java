package com.sap.sailing.domain.racelog.tracking;

import java.util.function.Consumer;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;


/**
 * Store abstraction for persistence of Fixes (e.g. GPSFix).
 */
public interface SensorFixStore {
    
    /**
     * Loads fixes for a device in a given time range.
     * 
     * @param consumer will be called for each loaded fix. Must not be <code>null</code>.
     * @param deviceIdentifier the device to load the fixes for. Must not be <code>null</code>.
     * @param start the lower bound of the time range to load. Must not be <code>null</code>.
     * @param end the upper bound of the time range to load. Must not be <code>null</code>.
     * @param inclusive true if fixes exactly at the bounds of the time range should be loaded, false otherwise.
     */
    <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier, TimePoint start, TimePoint end, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
    TransformationException;

    /**
     * Saves a fix for the given device and informs all registered listeners about the new fix.
     * 
     * @param device the device to store the fix for. Must not be <code>null</code>.
     * @param fix The fix to store. Must not be <code>null</code>.
     */
    <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix);

    /**
     * Listeners are notified, whenever a {@link GPSFix} submitted by the {@code device}
     * is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)} method.
     */
    void addListener(FixReceivedListener<? extends Timed> listener, DeviceIdentifier device);

    /**
     * Remove the registrations of the listener for all devices.
     */
    void removeListener(FixReceivedListener<? extends Timed> listener);
    
    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException;
}
