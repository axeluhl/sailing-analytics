package com.sap.sailing.server.gateway.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.json.simple.JSONObject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingNmeaDTOJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixNmeaDTOJsonSerializer;

public class Activator implements BundleActivator {
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private class GPSFixJsonSerializationService<T extends GPSFix> implements GPSFixJsonSerializationHandler {
        private final JsonDeserializer<T> deserializer;
        private final JsonSerializer<T> serializer;

        GPSFixJsonSerializationService(JsonDeserializer<T> deserializer, JsonSerializer<T> serializer) {
            this.deserializer = deserializer;
            this.serializer = serializer;
        }

        @SuppressWarnings("unchecked")
        @Override
        public JSONObject serialize(GPSFix fix) throws IllegalArgumentException {
            return serializer.serialize((T) fix);
        }

        @Override
        public T deserialize(JSONObject json) throws JsonDeserializationException {
            return deserializer.deserialize(json);
        }

    }

    private Dictionary<String, String> getDict(String type) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, type);
        return properties;
    }

    private <T extends GPSFix> void register(BundleContext context, JsonDeserializer<T> deserializer,
            JsonSerializer<T> serializer, String type) {
        registrations.add(context.registerService(GPSFixJsonSerializationHandler.class,
                new GPSFixJsonSerializationService<T>(deserializer, serializer), getDict(type)));
    }

    @Override
    public void start(BundleContext context) throws Exception {
        register(context, new GPSFixJsonDeserializer(), new GPSFixJsonSerializer(), GPSFixJsonDeserializer.TYPE);
        register(context, new GPSFixMovingJsonDeserializer(), new GPSFixMovingJsonSerializer(), GPSFixMovingJsonDeserializer.TYPE);
        register(context, new GPSFixNmeaDTOJsonDeserializer(), new GPSFixNmeaDTOJsonSerializer(), GPSFixNmeaDTOJsonDeserializer.TYPE);
        register(context, new GPSFixMovingNmeaDTOJsonDeserializer(), new GPSFixMovingNmeaDTOJsonSerializer(), GPSFixMovingNmeaDTOJsonDeserializer.TYPE);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }
}
