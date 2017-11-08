package com.sap.sailing.expeditionconnector;

import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.expeditionconnector.persistence.ExpeditionGpsDeviceIdentifier;

/**
 * Makes accessible a mapping from "Expedition" boat IDs to device UUIDs.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface DeviceRegistry {
    /**
     * If a device is registered for the boat ID {@code boatId}, a non-{@code null} device identifier is returned that
     * represents a virtual device for the GPS data received from that boat ID. Otherwise, {@code null} is returned.
     */
    ExpeditionGpsDeviceIdentifier getGpsDeviceIdentifier(int boatId);

    /**
     * If a device is registered for the boat ID {@code boatId}, a non-{@code null} device identifier is returned that
     * represents a virtual device for the additional non-GPS and non-wind sensor data received from that boat ID.
     * Otherwise, {@code null} is returned.
     */
    ExpeditionSensorDeviceIdentifier getSensorDeviceIdentifier(int boatId);
    
    SensorFixStore getSensorFixStore();
}
