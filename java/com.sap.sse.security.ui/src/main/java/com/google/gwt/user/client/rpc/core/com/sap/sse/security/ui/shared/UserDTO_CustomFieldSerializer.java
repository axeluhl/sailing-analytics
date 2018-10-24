package com.google.gwt.user.client.rpc.core.com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserDTO_CustomFieldSerializer extends CustomFieldSerializer<UserDTO> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, UserDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, UserDTO instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
        streamWriter.writeString(instance.getEmail());
        streamWriter.writeString(instance.getFullName());
        streamWriter.writeString(instance.getCompany());
        streamWriter.writeString(instance.getLocale());
        streamWriter.writeBoolean(instance.isEmailValidated());
        final ArrayList<Role> rolesAsList = new ArrayList<>();
        Util.addAll(instance.getRoles(), rolesAsList);
        streamWriter.writeObject(rolesAsList);
        streamWriter.writeObject(instance.getDefaultTenant());
        final ArrayList<WildcardPermission> permissionsAsList = new ArrayList<>();
        Util.addAll(instance.getPermissions(), permissionsAsList);
        streamWriter.writeObject(permissionsAsList);
        streamWriter.writeObject(instance.getAccounts());
        final ArrayList<UserGroup> groupsAsList = new ArrayList<>();
        Util.addAll(instance.getUserGroups(), groupsAsList);
        streamWriter.writeObject(groupsAsList);
        streamWriter.writeObject(instance.getOwnership());
        streamWriter.writeObject(instance.getAccessControlList());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public UserDTO instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static UserDTO instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final String name = streamReader.readString();
        final String email = streamReader.readString();
        final String fullName = streamReader.readString();
        final String company = streamReader.readString();
        final String locale = streamReader.readString();
        final boolean emailValidated = streamReader.readBoolean();
        @SuppressWarnings("unchecked")
        final ArrayList<Role> roles = (ArrayList<Role>) streamReader.readObject();
        final UserGroup defaultTenant = (UserGroup) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final ArrayList<WildcardPermission> permissions = (ArrayList<WildcardPermission>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final List<AccountDTO> accounts = (List<AccountDTO>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final ArrayList<UserGroup> groups = (ArrayList<UserGroup>) streamReader.readObject();
        final UserDTO userDTO = new UserDTO(name, email, fullName, company, locale, emailValidated, accounts, roles,
                defaultTenant, permissions, groups);
        userDTO.setOwnership((Ownership) streamReader.readObject());
        userDTO.setAccessControlList((AccessControlList) streamReader.readObject());
        return userDTO;
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, UserDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, UserDTO instance) {
        // Done by instantiateInstance
    }

}
