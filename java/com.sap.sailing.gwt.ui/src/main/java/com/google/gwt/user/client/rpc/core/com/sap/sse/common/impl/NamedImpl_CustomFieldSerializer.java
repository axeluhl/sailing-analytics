package com.google.gwt.user.client.rpc.core.com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.NamedImpl;

public class NamedImpl_CustomFieldSerializer extends CustomFieldSerializer<NamedImpl> {
    public static void deserialize(SerializationStreamReader streamReader, com.sap.sse.common.impl.NamedImpl instance)
            throws SerializationException {
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public NamedImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return new NamedImpl(streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, NamedImpl instance)
            throws SerializationException {
        // done by instantiateInstance
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, NamedImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
    }

}
