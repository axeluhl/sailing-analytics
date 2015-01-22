package com.sap.sailing.domain.racelogtracking;

import java.util.UUID;


public interface SmartphoneUUIDIdentifier extends DeviceIdentifier {
    String TYPE = "smartphoneUUID";
    
    UUID getUUID();
}
