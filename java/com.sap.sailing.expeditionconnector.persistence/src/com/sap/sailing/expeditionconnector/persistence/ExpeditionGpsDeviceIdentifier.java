package com.sap.sailing.expeditionconnector.persistence;

import com.sap.sailing.expeditionconnector.ExpeditionDeviceIdentifier;

/**
 * A device identifier used to identify an installation of the regatta tool "Expedition"
 * regarding its basic GPS information.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ExpeditionGpsDeviceIdentifier extends ExpeditionDeviceIdentifier {
    public static final String TYPE = "EXPEDITION_GPS";
}
