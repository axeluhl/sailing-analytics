package com.sap.sailing.domain.test.mock;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.devices.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockDeviceTypeServiceFinderFactory implements TypeBasedServiceFinderFactory {
    private final MockDeviceTypeServiceFinder<DeviceIdentifierPersistenceHandler> serviceFinder = new MockDeviceTypeServiceFinder<>();

    @SuppressWarnings("unchecked")
    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
        if (clazz.equals(DeviceIdentifierPersistenceHandler.class)) {
            return (TypeBasedServiceFinder<ServiceT>) serviceFinder;
        }
        return null;
    }
}
