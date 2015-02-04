package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockEmptyServiceFinderFactory implements TypeBasedServiceFinderFactory {
    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
        return new MockEmptyServiceFinder<ServiceT>();
    }
}
