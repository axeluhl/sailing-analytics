package com.sap.sailing.domain.racelog.tracking.test.mock;

import java.util.Collections;
import java.util.Set;

import com.sap.sse.common.TypeBasedServiceFinder;

public class MockServiceFinder<T> implements TypeBasedServiceFinder<T> {
    private final T handler;
    private T fallback;
    
    public MockServiceFinder(T handler) {
        this.handler = handler;
    }

    @Override
    public T findService(String deviceType) {
        if (deviceType.equals(SmartphoneImeiIdentifier.TYPE)) return handler;
        return fallback;
    }

    @Override
    public void setFallbackService(T fallback) {
        this.fallback = fallback;
    }
    
    @Override
    public Set<T> findAllServices() {
        return Collections.singleton(handler);
    }
}
