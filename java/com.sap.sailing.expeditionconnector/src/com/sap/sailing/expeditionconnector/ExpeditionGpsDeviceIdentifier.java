package com.sap.sailing.expeditionconnector;

/**
 * A device identifier used to identify an installation of the regatta tool "Expedition"
 * regarding its basic GPS information.
 * 
 * @author Fredrik Teschke
 *
 */
public interface ExpeditionGpsDeviceIdentifier extends ExpeditionDeviceIdentifier {
    public static final String TYPE = "EXPEDITION_GPS";
}
