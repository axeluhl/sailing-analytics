package com.sap.sailing.domain.smartphoneadapter.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;

/**
 * Registers an instance of {@link SmartphoneImeiHandlerImpl} as both, a {@link RaceLogTrackingDeviceHandler} as well as a
 * {@link DeviceIdentifierPersistenceHandler} for devices of type {@link SmartphoneImeiIdentifier#TYPE}.
 * 
 * @author Fredrik Teschke
 * 
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        SmartphoneImeiHandlerImpl handler = new SmartphoneImeiHandlerImpl();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(DeviceIdentifier.TYPE, SmartphoneImeiIdentifier.TYPE);
        context.registerService(DeviceIdentifierPersistenceHandler.class, handler, properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
