package com.sap.sailing.domain.racelog.tracking;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
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
     * @param consumer
     *            will be called for each loaded fix. Must not be <code>null</code>.
     * @param deviceIdentifier
     *            the device to load the fixes for. Must not be <code>null</code>.
     * @param start
     *            the lower bound of the time range to load. If <code>null</code>, fixes are loaded from
     *            {@link TimePoint#BeginningOfTime}.
     * @param end
     *            the upper bound of the time range to load. If <code>null</code>, fixes are loaded to
     *            {@link TimePoint#EndOfTime}.
     * @param toIsInclusive
     *            true if fixes exactly at the {@code end} bounds of the time range should be loaded, false otherwise.
     *            Fixes exactly on the {@code start} bounds are always loaded.
     */
    <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier, TimePoint start, TimePoint end,
            boolean toIsInclusive) throws NoCorrespondingServiceRegisteredException,
    TransformationException;
    
    /**
     * Loads fixes for a device in a given time range.
     * 
     * @param consumer will be called for each loaded fix. Must not be <code>null</code>.
     * @param deviceIdentifier the device to load the fixes for. Must not be <code>null</code>.
     * @param start the lower bound of the time range to load. If <code>null</code>, fixes are loaded from {@link TimePoint#BeginningOfTime}.
     * @param end the upper bound of the time range to load. If <code>null</code>, fixes are loaded to {@link TimePoint#EndOfTime}.
     * @param inclusive true if fixes exactly at the bounds of the time range should be loaded, false otherwise.
     * @param progressReporter not allowed to be null, can be used to get reports of the approximate loading progress
     */
    <FixT extends Timed> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier, TimePoint start,
            TimePoint end, boolean inclusive, BooleanSupplier isPreemptiveStopped,
            Consumer<Double> progressReporter)
            throws NoCorrespondingServiceRegisteredException,
    TransformationException;

    /**
     * Saves a single fix for the given device and informs all registered listeners about the new fix.
     * 
     * @param device
     *            the device to store the fix for. Must not be <code>null</code>.
     * @param fix
     *            The fix to store. Must not be <code>null</code>.
     */
    <FixT extends Timed> void storeFix(DeviceIdentifier device, FixT fix);

    /**
     * Saves a batch of fixes for the given device and informs all registered listeners about the new fix.
     * 
     * @param device
     *            the device to store the fix for. Must not be <code>null</code>.
     * @param fix
     *            The fix to store. Must not be <code>null</code>.
     * @return An Iterable with RegattaAndRaceIdentifier is returned, that will contain races with new maneuvers, which
     *         were not available at the last time the given device stored a fix, the Iterable can be empty. It can also
     *         contain multiple identifiers, if the devicemapping is currently ambiguous
     */
    <FixT extends Timed> Iterable<RegattaAndRaceIdentifier> storeFixes(DeviceIdentifier device, Iterable<FixT> fixes);

    /**
     * Listeners are notified, whenever a {@link GPSFix} submitted by the {@code device}
     * is stored through the {@link #storeFix(DeviceIdentifier, GPSFix)} method.
     */
    void addListener(FixReceivedListener<? extends Timed> listener, DeviceIdentifier device);

    /**
     * Remove the registrations of the listener for all devices.
     */
    void removeListener(FixReceivedListener<? extends Timed> listener);
    
    /**
     * Remove the registrations of the listener for the given device.
     */
    void removeListener(FixReceivedListener<? extends Timed> listener, DeviceIdentifier device);
    
    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) throws TransformationException,
    NoCorrespondingServiceRegisteredException;
    
    long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException;
    
    <FixT extends Timed> Map<DeviceIdentifier, FixT> getLastFix(Iterable<DeviceIdentifier> forDevices) throws TransformationException, NoCorrespondingServiceRegisteredException;

    /**
     * Loads the youngest fix for the given device in the specified {@link TimeRange}.
     * 
     * @return true if a fix was loaded, false otherwise
     */
    <FixT extends Timed> boolean loadOldestFix(Consumer<FixT> consumer, DeviceIdentifier device,
            TimeRange timeRangetoLoad)
            throws NoCorrespondingServiceRegisteredException, TransformationException;

    /**
     * Loads the oldest fix for the given device in the specified {@link TimeRange}.
     * 
     * @return true if a fix was loaded, false otherwise
     */
    <FixT extends Timed> boolean loadYoungestFix(Consumer<FixT> consumer, DeviceIdentifier device,
            TimeRange timeRangetoLoad)
            throws NoCorrespondingServiceRegisteredException, TransformationException;
}
