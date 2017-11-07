package com.sap.sailing.expeditionconnector.persistence;

import java.util.UUID;

import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;

public interface ExpeditionDeviceIdentifier extends DeviceIdentifier {
    /**
     * Derived from an {@link ExpeditionDeviceConfiguration#getDeviceUuid()}.
     */
    UUID getId();
}
