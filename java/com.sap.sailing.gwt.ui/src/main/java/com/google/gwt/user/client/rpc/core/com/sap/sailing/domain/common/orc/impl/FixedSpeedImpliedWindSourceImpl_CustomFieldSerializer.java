package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.orc.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.orc.impl.FixedSpeedImpliedWindSourceImpl;
import com.sap.sse.common.Speed;

public class FixedSpeedImpliedWindSourceImpl_CustomFieldSerializer extends CustomFieldSerializer<FixedSpeedImpliedWindSourceImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, FixedSpeedImpliedWindSourceImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, FixedSpeedImpliedWindSourceImpl instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getFixedImpliedWindSpeed());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public FixedSpeedImpliedWindSourceImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static FixedSpeedImpliedWindSourceImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        final Speed fixedImpliedWindSpeed = (Speed) streamReader.readObject();
        return new FixedSpeedImpliedWindSourceImpl(fixedImpliedWindSpeed);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, FixedSpeedImpliedWindSourceImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, FixedSpeedImpliedWindSourceImpl instance) {
        // Done by instantiateInstance
    }

}
