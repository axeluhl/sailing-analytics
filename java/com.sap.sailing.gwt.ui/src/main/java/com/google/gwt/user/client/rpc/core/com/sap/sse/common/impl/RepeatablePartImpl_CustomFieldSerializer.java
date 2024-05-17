package com.google.gwt.user.client.rpc.core.com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.RepeatablePartImpl;

public class RepeatablePartImpl_CustomFieldSerializer extends CustomFieldSerializer<RepeatablePartImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, RepeatablePartImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, RepeatablePartImpl instance)
            throws SerializationException {
        streamWriter.writeInt(instance.getZeroBasedIndexOfRepeatablePartStart());
        streamWriter.writeInt(instance.getZeroBasedIndexOfRepeatablePartEnd());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public RepeatablePartImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static RepeatablePartImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new RepeatablePartImpl(streamReader.readInt(), streamReader.readInt());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, RepeatablePartImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, RepeatablePartImpl instance) {
        // Done by instantiateInstance
    }

}
