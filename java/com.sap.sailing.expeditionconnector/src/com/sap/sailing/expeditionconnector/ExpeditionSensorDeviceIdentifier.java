package com.sap.sailing.expeditionconnector;

/**
 * A device identifier used to identify an installation of the regatta tool "Expedition"
 * regarding its sensor data beyond the basic GPS and wind information.
 * 
 * @author Axel Uhl
 *
 */
public interface ExpeditionSensorDeviceIdentifier extends ExpeditionDeviceIdentifier {
    public static final String TYPE = "EXPEDITION_SENSOR";
}
