package com.google.gwt.user.client.rpc.core.com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.NamedImpl;

public class NamedImpl_CustomFieldSerializer extends CustomFieldSerializer<NamedImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, NamedImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, NamedImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public NamedImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static NamedImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new NamedImpl(streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, NamedImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, NamedImpl instance) {
        // Done by instantiateInstance
    }

}
