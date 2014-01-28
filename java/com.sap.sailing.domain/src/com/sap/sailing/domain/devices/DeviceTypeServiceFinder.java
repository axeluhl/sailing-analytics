package com.sap.sailing.domain.devices;

public interface DeviceTypeServiceFinder {
    <ServiceType> ServiceType findService(Class<ServiceType> clazz, String deviceType);
}
