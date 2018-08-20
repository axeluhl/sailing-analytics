package com.sap.sailing.domain.racelog.tracking.test.mock;

import java.util.Collections;
import java.util.Set;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;

public class MockEmptyServiceFinder<ServiceType> extends AbstractTypeBasedServiceFinder<ServiceType> {
    private ServiceType fallback;

    @Override
    public ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException {
        return fallback;
    }

    @Override
    public void setFallbackService(ServiceType fallback) {
        this.fallback = fallback;
    }

    @Override
    public Set<ServiceType> findAllServices() {
        return Collections.singleton(fallback);
    }
}
