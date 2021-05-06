package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class SecuredSecurityTypes_CustomFieldSerializer extends CustomFieldSerializer<SecuredSecurityTypes> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, SecuredSecurityTypes instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, SecuredSecurityTypes instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public SecuredSecurityTypes instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static SecuredSecurityTypes instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final String name = streamReader.readString();
        return Util.filter(SecuredSecurityTypes.getAllInstances(), sst->sst.getName().equals(name)).iterator().next();
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, SecuredSecurityTypes instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, SecuredSecurityTypes instance) {
        // Done by instantiateInstance
    }

}
