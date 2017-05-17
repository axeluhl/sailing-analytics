package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sse.common.TypeBasedServiceFinder;

public abstract class AbstractTypeBasedServiceFinder<ServiceT> implements TypeBasedServiceFinder<ServiceT> {
    @Override
    public void applyServiceWhenAvailable(String type, com.sap.sse.common.TypeBasedServiceFinder.Callback<ServiceT> callback) {
        final ServiceT service = findService(type);
        callback.withService(service);
    }
}
