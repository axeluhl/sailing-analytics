package com.sap.sailing.domain.abstractlog.race.tracking;

import java.util.UUID;


public interface SmartphoneUUIDIdentifier extends DeviceIdentifier {
    String TYPE = "smartphoneUUID";
    
    UUID getUUID();
}
