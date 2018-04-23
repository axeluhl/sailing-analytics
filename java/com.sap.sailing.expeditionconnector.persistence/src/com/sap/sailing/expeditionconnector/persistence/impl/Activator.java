package com.sap.sailing.expeditionconnector.persistence.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;
import com.sap.sailing.expeditionconnector.persistence.ExpeditionGpsDeviceIdentifier;
import com.sap.sse.MasterDataImportClassLoaderService;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.mongodb.MongoDBService;

public class Activator implements BundleActivator {

    private static BundleContext context;

    /**
     * Registrations of OSGi services to be de-registered when the bundle shuts down
     */
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        registrations.add(context.registerService(DeviceIdentifierMongoHandler.class, new ExpeditionGpsDeviceIdentifierMongoHandler(), getDict(ExpeditionGpsDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierMongoHandler.class, new ExpeditionSensorDeviceIdentifierMongoHandler(), getDict(ExpeditionSensorDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(MasterDataImportClassLoaderService.class, new MasterDataImportClassLoaderServiceImpl(), null));
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

    private Dictionary<String, String> getDict(String type) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, type);
        return properties;
    }
}
