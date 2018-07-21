package com.sap.sailing.domain.racelog.tracking.test.mock;

import java.util.Collections;
import java.util.Set;

public class MockServiceFinder<T> extends AbstractTypeBasedServiceFinder<T> {
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
