package com.sap.sailing.domain.test.mock;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.devices.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;
import com.sap.sailing.domain.persistence.devices.GPSFixPersistenceHandler;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockServiceFinderFactory implements TypeBasedServiceFinderFactory {
	Map<Class<?>, TypeBasedServiceFinder<?>> serviceFinders = new HashMap<>();
	
	public MockServiceFinderFactory() {
		serviceFinders.put(DeviceIdentifierPersistenceHandler.class, new MockDeviceTypeServiceFinder<>());
		serviceFinders.put(DeviceIdentifierJsonSerializationHandler.class, new MockDeviceTypeServiceFinder<>());
		serviceFinders.put(GPSFixPersistenceHandler.class, new MockGPSFixPersistenceServiceFinder());
		serviceFinders.put(GPSFixJsonSerializationHandler.class, new MockGPSFixJsonSerializationServiceFinder());
	}
    
    @SuppressWarnings("unchecked")
    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
    	return (TypeBasedServiceFinder<ServiceT>) serviceFinders.get(clazz);
    }
}
