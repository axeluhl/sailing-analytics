package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.AdminRole;

public class AdminRole_CustomFieldSerializer extends CustomFieldSerializer<AdminRole> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, AdminRole instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, AdminRole instance)
            throws SerializationException {
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public AdminRole instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static AdminRole instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return AdminRole.getInstance();
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, AdminRole instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, AdminRole instance) {
        // Done by instantiateInstance
    }

}
