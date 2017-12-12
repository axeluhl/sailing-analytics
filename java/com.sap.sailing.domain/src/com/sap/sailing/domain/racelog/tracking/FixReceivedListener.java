package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.Timed;

/**
 * Listener to be informed about new fixes by {@link SensorFixStore}.
 *
 * @param <FixT> the type of fixes this listener can consume.
 */
public interface FixReceivedListener<FixT extends Timed> {
    void fixReceived(DeviceIdentifier device, FixT fix);
}
