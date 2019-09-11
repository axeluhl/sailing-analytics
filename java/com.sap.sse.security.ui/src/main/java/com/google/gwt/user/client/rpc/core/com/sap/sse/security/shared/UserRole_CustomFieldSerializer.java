package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.UserRole;

public class UserRole_CustomFieldSerializer extends CustomFieldSerializer<UserRole> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UserRole instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UserRole instance)
            throws SerializationException {
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UserRole instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UserRole instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return UserRole.getInstance();
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UserRole instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, RolePrototype instance) {
        // Done by instantiateInstance
    }

}
