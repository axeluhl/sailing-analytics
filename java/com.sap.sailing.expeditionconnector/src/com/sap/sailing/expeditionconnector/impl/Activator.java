package com.sap.sailing.expeditionconnector.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.racelogtracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.expeditionconnector.ExpeditionGpsDeviceIdentifier;
import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;
import com.sap.sailing.expeditionconnector.ExpeditionTrackerFactory;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sse.common.TypeBasedServiceFinder;

public class Activator implements BundleActivator {
    private static Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String EXPEDITION_UDP_PORT_PROPERTY_NAME = "expedition.udp.port";
    
    private static Activator instance;
    
    /**
     * Registrations of OSGi services to be de-registered when the bundle shuts down
     */
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private static final int DEFAULT_PORT = 2013;
    
    private int port;
    
    public Activator() {
        port = Integer.valueOf(System.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME, ""+DEFAULT_PORT));
        logger.log(Level.INFO, "setting default for "+EXPEDITION_UDP_PORT_PROPERTY_NAME+" to "+port);
    }
    
    private Dictionary<String, String> getDict(String type) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, type);
        return properties;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        if (instance == null) {
            instance = this;
        }
        if (context.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME) != null) {
            port = Integer.valueOf(context.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME));
            logger.log(Level.INFO, "found "+EXPEDITION_UDP_PORT_PROPERTY_NAME+"="+port+" in OSGi context");
        }
        // register the Expedition wind tracker factory as an OSGi service
        registrations.add(context.registerService(ExpeditionTrackerFactory.class, ExpeditionTrackerFactory.getInstance(), /* properties */null));
        registrations.add(context.registerService(WindTrackerFactory.class, ExpeditionTrackerFactory.getInstance(), /* properties */null));
        
        registrations.add(context.registerService(DeviceIdentifierJsonHandler.class, new ExpeditionGpsDeviceIdentifierJsonHandler(), getDict(ExpeditionGpsDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierStringSerializationHandler.class, new ExpeditionGpsStringSerializationHandler(), getDict(ExpeditionGpsDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierJsonHandler.class, new ExpeditionSensorDeviceIdentifierJsonHandler(), getDict(ExpeditionSensorDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierStringSerializationHandler.class, new ExpeditionSensorStringSerializationHandler(), getDict(ExpeditionSensorDeviceIdentifier.TYPE)));
    }
    
    public static Activator getInstance() {
        if (instance == null) {
            instance = new Activator();
        }
        return instance;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }
    
    public int getExpeditionUDPPort() {
        return port;
    }

}
