package com.sap.sailing.domain.racelogtracking;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;

/**
 * A device identifier used to identify a non-existent, virtual device, which is used only once as an
 * identifier to add a single fix to a track (i.e. pinging the location of that track's item).
 * 
 * @author Fredrik Teschke
 *
 */
public interface PingDeviceIdentifier extends DeviceIdentifier {
    public static final String TYPE = "PING";
    UUID getId();
}
