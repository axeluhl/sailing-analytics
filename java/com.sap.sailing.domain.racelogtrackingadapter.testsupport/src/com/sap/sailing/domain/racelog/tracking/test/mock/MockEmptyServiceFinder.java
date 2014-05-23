package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;

public class MockEmptyServiceFinder<ServiceType> implements TypeBasedServiceFinder<ServiceType> {
    private ServiceType fallback;

    @Override
    public ServiceType findService(String type) throws NoCorrespondingServiceRegisteredException {
        return fallback;
    }

    @Override
    public void setFallbackService(ServiceType fallback) {
        this.fallback = fallback;
    }

}
