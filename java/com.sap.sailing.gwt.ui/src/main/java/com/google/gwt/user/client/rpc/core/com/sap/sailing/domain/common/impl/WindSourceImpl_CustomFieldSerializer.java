package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;

public class WindSourceImpl_CustomFieldSerializer extends CustomFieldSerializer<WindSourceImpl> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public WindSourceImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static WindSourceImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new WindSourceImpl(WindSourceType.values()[streamReader.readInt()]);
    }

    public static void deserialize(SerializationStreamReader streamReader, WindSourceImpl instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, WindSourceImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, WindSourceImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, WindSourceImpl instance)
            throws SerializationException {
        streamWriter.writeInt(instance.getType().ordinal());
    }

}
