package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared.impl;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.impl.UserGroupImpl;

public class UserGroupImpl_CustomFieldSerializer extends CustomFieldSerializer<UserGroupImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UserGroupImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UserGroupImpl instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getId());
        streamWriter.writeString(instance.getName());
        final ArrayList<SecurityUser> usersAsList = new ArrayList<>();
        Util.addAll(instance.getUsers(), usersAsList);
        streamWriter.writeObject(usersAsList);
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UserGroupImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UserGroupImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final UUID id = (UUID) streamReader.readObject();
        final String name = streamReader.readString();
        @SuppressWarnings("unchecked")
        final ArrayList<SecurityUser> users = (ArrayList<SecurityUser>) streamReader.readObject();
        return new UserGroupImpl(id, name, users);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UserGroupImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, UserGroupImpl instance) {
        // Done by instantiateInstance
    }

}
