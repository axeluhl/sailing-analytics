package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared.impl;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecurityUserImpl;

public class SecurityUserImpl_CustomFieldSerializer extends CustomFieldSerializer<SecurityUserImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, SecurityUserImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, SecurityUserImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
        final ArrayList<Role> rolesAsList = new ArrayList<>();
        Util.addAll(instance.getRoles(), rolesAsList);
        streamWriter.writeObject(rolesAsList);
        final ArrayList<WildcardPermission> permissionsAsList = new ArrayList<>();
        Util.addAll(instance.getPermissions(), permissionsAsList);
        streamWriter.writeObject(permissionsAsList);
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public SecurityUserImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static SecurityUserImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final String name = streamReader.readString();
        @SuppressWarnings("unchecked")
        final ArrayList<Role> roles = (ArrayList<Role>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final ArrayList<WildcardPermission> permissions = (ArrayList<WildcardPermission>) streamReader.readObject();
        return new SecurityUserImpl(name, roles, permissions);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, SecurityUserImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, SecurityUserImpl instance) {
        // Done by instantiateInstance
    }

}
