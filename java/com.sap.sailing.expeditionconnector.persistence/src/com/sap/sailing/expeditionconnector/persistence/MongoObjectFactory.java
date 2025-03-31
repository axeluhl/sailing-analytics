package com.sap.sailing.expeditionconnector.persistence;

import java.util.UUID;

import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;

public interface MongoObjectFactory {
    void storeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration);

    void removeExpeditionDeviceConfiguration(UUID expeditionDeviceConfigurationId);
}
