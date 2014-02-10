package com.sap.sailing.domain.common.racelog.tracking;

public interface TypeBasedServiceFinderFactory {
    <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz);
}
