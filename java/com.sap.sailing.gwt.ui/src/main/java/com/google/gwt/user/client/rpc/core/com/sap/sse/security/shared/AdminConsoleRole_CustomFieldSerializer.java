package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.gwt.ui.server.AdminConsoleRole;

public class AdminConsoleRole_CustomFieldSerializer extends CustomFieldSerializer<AdminConsoleRole> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, AdminConsoleRole instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, AdminConsoleRole instance)
            throws SerializationException {
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public AdminConsoleRole instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static AdminConsoleRole instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return AdminConsoleRole.getInstance();
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, AdminConsoleRole instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, AdminConsoleRole instance) {
        // Done by instantiateInstance
    }

}
