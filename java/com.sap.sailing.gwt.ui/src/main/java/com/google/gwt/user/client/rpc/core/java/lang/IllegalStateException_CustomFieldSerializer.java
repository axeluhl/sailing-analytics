package com.google.gwt.user.client.rpc.core.java.lang;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class IllegalStateException_CustomFieldSerializer extends CustomFieldSerializer<IllegalStateException> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public IllegalStateException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static IllegalStateException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new IllegalStateException(streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, IllegalStateException instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, IllegalStateException instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, IllegalStateException instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, IllegalStateException instance)
            throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

}