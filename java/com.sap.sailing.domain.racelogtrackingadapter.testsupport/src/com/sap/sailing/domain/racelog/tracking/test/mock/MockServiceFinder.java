package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;

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
}
