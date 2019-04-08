package com.sap.sailing.expeditionconnector.persistence;

import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;

public interface MongoObjectFactory {
    void storeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration);

    void removeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration);
}
