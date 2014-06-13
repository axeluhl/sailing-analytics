package com.google.gwt.user.client.rpc.core.java.util;

import java.util.UUID;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Serializer for emulated {@link UUID}.
 */
public class UUID_CustomFieldSerializer extends CustomFieldSerializer<UUID> {

    public static void serialize(SerializationStreamWriter streamWriter, UUID instance) throws SerializationException {
        streamWriter.writeString(instance.toString());
    }
    
    public static UUID instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return UUID.fromString(streamReader.readString());
    }
    
    
    public static boolean hasCustomInstantiate() {
        return true;
    }
    
    public static void deserialize(SerializationStreamReader streamReader, UUID instance)
            throws SerializationException {
        // no operation
    }
    
    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UUID instance) throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    @Override
    public UUID instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }
    
    @Override
    public boolean hasCustomInstantiateInstance() {
        return hasCustomInstantiate();
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UUID instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

}
