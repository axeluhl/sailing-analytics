package com.google.gwt.user.client.rpc.core.com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class DegreeBearingImpl_CustomFieldSerializer extends CustomFieldSerializer<DegreeBearingImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, DegreeBearingImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, DegreeBearingImpl instance)
            throws SerializationException {
        streamWriter.writeDouble(instance.getDegrees());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public DegreeBearingImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static DegreeBearingImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new DegreeBearingImpl(streamReader.readDouble());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, DegreeBearingImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, DegreeBearingImpl instance) {
        // Done by instantiateInstance
    }

}
