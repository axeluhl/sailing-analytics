package com.sap.sailing.domain.smartphoneadapter.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;

/**
 * Registers an instance of {@link SmartphoneImeiHandlerImpl} as both, a {@link RaceLogTrackingDeviceHandler} as well as a
 * {@link DeviceIdentifierPersistenceHandler} for devices of type {@link SmartphoneImeiIdentifier#TYPE}.
 * 
 * @author Fredrik Teschke
 * 
 */
public class Activator implements BundleActivator {
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();
    
    @Override
    public void start(BundleContext context) throws Exception {
        SmartphoneImeiHandlerImpl handler = new SmartphoneImeiHandlerImpl();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, SmartphoneImeiIdentifier.TYPE);
        registrations.add(context.registerService(DeviceIdentifierPersistenceHandler.class, handler, properties));
        registrations.add(context.registerService(DeviceIdentifierJsonSerializationHandler.class, handler, properties));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }
}
