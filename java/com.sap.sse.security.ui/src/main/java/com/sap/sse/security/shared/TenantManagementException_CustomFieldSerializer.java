package com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class TenantManagementException_CustomFieldSerializer extends CustomFieldSerializer<TenantManagementException> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public TenantManagementException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static TenantManagementException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new TenantManagementException(streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, TenantManagementException instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, TenantManagementException instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, TenantManagementException instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, TenantManagementException instance)
            throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

}