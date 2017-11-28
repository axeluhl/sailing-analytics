package com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class UnauthorizedException_CustomFieldSerializer extends CustomFieldSerializer<UnauthorizedException> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UnauthorizedException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UnauthorizedException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new UnauthorizedException(streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, UnauthorizedException instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UnauthorizedException instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UnauthorizedException instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UnauthorizedException instance)
            throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

}