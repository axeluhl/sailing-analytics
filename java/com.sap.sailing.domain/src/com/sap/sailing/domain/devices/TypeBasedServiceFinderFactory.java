package com.sap.sailing.domain.devices;

public interface TypeBasedServiceFinderFactory {
    <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz);
}
