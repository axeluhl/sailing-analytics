package com.sap.sailing.expeditionconnector.persistence;

import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;

public interface DomainObjectFactory {
    Iterable<ExpeditionDeviceConfiguration> getExpeditionDeviceConfigurations();
}
