package com.sap.sailing.domain.racelog.tracking;

import java.util.UUID;


public interface SmartphoneUUIDIdentifier extends DeviceIdentifier {
    String TYPE = "smartphoneUUID";
    
    UUID getUUID();
}
