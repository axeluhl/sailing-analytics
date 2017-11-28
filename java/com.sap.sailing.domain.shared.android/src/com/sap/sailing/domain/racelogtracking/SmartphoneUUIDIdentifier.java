package com.sap.sailing.domain.racelogtracking;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;


public interface SmartphoneUUIDIdentifier extends DeviceIdentifier {
    String TYPE = "smartphoneUUID";
    
    UUID getUUID();
}
