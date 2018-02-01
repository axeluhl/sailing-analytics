package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;

public class WindSourceWithAdditionalID_CustomFieldSerializer extends CustomFieldSerializer<WindSourceWithAdditionalID> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public WindSourceWithAdditionalID instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static WindSourceWithAdditionalID instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new WindSourceWithAdditionalID(WindSourceType.values()[streamReader.readInt()], streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, WindSourceWithAdditionalID instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, WindSourceWithAdditionalID instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, WindSourceWithAdditionalID instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, WindSourceWithAdditionalID instance)
            throws SerializationException {
        streamWriter.writeInt(instance.getType().ordinal());
        streamWriter.writeString(instance.getId());
    }

}
