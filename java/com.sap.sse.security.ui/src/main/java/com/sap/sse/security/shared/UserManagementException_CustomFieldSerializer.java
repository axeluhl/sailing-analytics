package com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class UserManagementException_CustomFieldSerializer extends CustomFieldSerializer<UserManagementException> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UserManagementException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UserManagementException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new UserManagementException(streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, UserManagementException instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UserManagementException instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UserManagementException instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UserManagementException instance)
            throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

}
