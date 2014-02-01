package com.sap.sailing.domain.devices;

public interface TypeBasedServiceFinder<ServiceType> {
    public static final String TYPE = "type";
    ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException;
}
