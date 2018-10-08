package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.Timed;

/**
 * Listener to be informed about new fixes by {@link SensorFixStore}.
 *
 * @param <FixT>
 *            the type of fixes this listener can consume.
 */
public interface FixReceivedListener<FixT extends Timed> {
    /**
     * 
     * @param device
     *            the device that recorded the fix. Cannot be <code>null</code>.
     * @param fix
     *            The fix that was stored. Cannot be <code>null</code>.
     * @return An {@link Iterable} with {@link RegattaAndRaceIdentifier}s is returned that will contain races with new
     *         maneuvers which were not available at the last time the given device stored a fix. The {@link Iterable}
     *         returned can be empty but is never {@code null}. It can also contain multiple identifiers if the device
     *         mapping is currently ambiguous.
     */
    Iterable<RegattaAndRaceIdentifier> fixReceived(DeviceIdentifier device, FixT fix);
}
