package com.sap.sailing.expeditionconnector;

import java.util.UUID;

import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;

public interface ExpeditionDeviceIdentifier extends DeviceIdentifier {
    /**
     * Derived from an {@link ExpeditionDeviceConfiguration#getDeviceUuid()}.
     */
    UUID getId();
}
