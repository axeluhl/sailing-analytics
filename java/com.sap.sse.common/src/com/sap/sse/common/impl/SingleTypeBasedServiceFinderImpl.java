package com.sap.sse.common.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;

/**
 * Only can provide the service for a single {@code type}, e.g. on smartphones with no OSGi context.
 * Optionally, a {@link #setFallbackService(Object) fallback} can be set.
 * @author Fredrik Teschke
 */
public class SingleTypeBasedServiceFinderImpl<ServiceType> implements
TypeBasedServiceFinder<ServiceType> {
    private final ServiceType service;
    private final String type;
    private ServiceType fallback;

    public SingleTypeBasedServiceFinderImpl(ServiceType service, ServiceType fallback, String type) {
        this.service = service;
        this.type = type;
        this.fallback = fallback;
    }

    public SingleTypeBasedServiceFinderImpl(ServiceType service, String type) {
        this(service, null, type);
    }

    @Override
    public ServiceType findService(String type)
            throws NoCorrespondingServiceRegisteredException {
        if (type.equals(this.type)) return service;
        if (fallback != null) return fallback;
        throw new NoCorrespondingServiceRegisteredException("Only one service registered", type, service.getClass().getSimpleName());
    }

    @Override
    public void setFallbackService(ServiceType fallback) {
        this.fallback = fallback;
    }
    
    @Override
    public Set<ServiceType> findAllServices() {
        return Collections.singleton(service);
    }
}
