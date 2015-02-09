package com.sap.sailing.domain.racelogtracking.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.SmartphoneUUIDIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingNmeaDTOJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixNmeaDTOJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.GPSFixJsonHandlerImpl;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.SmartphoneUUIDJsonHandler;
import com.sap.sse.common.TypeBasedServiceFinder;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
	
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private Dictionary<String, String> getDict(String type) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, type);
        return properties;
    }

    private <T extends GPSFix> void registerGPSFixJsonService(BundleContext context, JsonDeserializer<T> deserializer,
            JsonSerializer<T> serializer, String type) {
        registrations.add(context.registerService(GPSFixJsonHandler.class,
                new GPSFixJsonHandlerImpl<T>(deserializer, serializer), getDict(type)));
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        registrations.add(context.registerService(DeviceIdentifierMongoHandler.class, new SmartphoneUUIDMongoHandler(), getDict(SmartphoneUUIDIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierJsonHandler.class, new SmartphoneUUIDJsonHandler(), getDict(SmartphoneUUIDIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierStringSerializationHandler.class, new SmartphoneUUIDStringSerializationHandler(), getDict(SmartphoneUUIDIdentifier.TYPE)));
        
        registrations.add(context.registerService(DeviceIdentifierMongoHandler.class, new PingDeviceIdentifierMongoHandler(), getDict(PingDeviceIdentifierImpl.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierJsonHandler.class, new PingDeviceIdentifierJsonHandler(), getDict(PingDeviceIdentifierImpl.TYPE)));
        
        registrations.add(context.registerService(DeviceIdentifierMongoHandler.class, new TrackFileImportDeviceIdentifierMongoHandler(), getDict(TrackFileImportDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierJsonHandler.class, new TrackFileImportDeviceIdentifierJsonHandler(), getDict(TrackFileImportDeviceIdentifier.TYPE)));
        registrations.add(context.registerService(DeviceIdentifierStringSerializationHandler.class, new TrackFileImportDeviceIdentifierStringSerializationHandler(), getDict(TrackFileImportDeviceIdentifier.TYPE)));
        
        registerGPSFixJsonService(context, new GPSFixJsonDeserializer(), new GPSFixJsonSerializer(), GPSFixJsonDeserializer.TYPE);
        registerGPSFixJsonService(context, new GPSFixMovingJsonDeserializer(), new GPSFixMovingJsonSerializer(), GPSFixMovingJsonDeserializer.TYPE);
        registerGPSFixJsonService(context, new GPSFixNmeaDTOJsonDeserializer(), new GPSFixNmeaDTOJsonSerializer(), GPSFixNmeaDTOJsonDeserializer.TYPE);
        registerGPSFixJsonService(context, new GPSFixMovingNmeaDTOJsonDeserializer(), new GPSFixMovingNmeaDTOJsonSerializer(), GPSFixMovingNmeaDTOJsonDeserializer.TYPE);
        
        registrations.add(context.registerService(RaceLogTrackingAdapterFactory.class, RaceLogTrackingAdapterFactory.INSTANCE, null));
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }
}
