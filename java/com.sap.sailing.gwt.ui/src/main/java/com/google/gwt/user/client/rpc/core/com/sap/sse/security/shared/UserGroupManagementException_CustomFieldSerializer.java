package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.UserGroupManagementException;

public class UserGroupManagementException_CustomFieldSerializer extends CustomFieldSerializer<UserGroupManagementException> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UserGroupManagementException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UserGroupManagementException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new UserGroupManagementException(streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, UserGroupManagementException instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UserGroupManagementException instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UserGroupManagementException instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UserGroupManagementException instance)
            throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

}