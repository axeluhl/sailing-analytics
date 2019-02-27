package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class TypeRelativeObjectIdentifier_CustomFieldSerializer extends CustomFieldSerializer<TypeRelativeObjectIdentifier> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, TypeRelativeObjectIdentifier instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, TypeRelativeObjectIdentifier instance)
            throws SerializationException {
        streamWriter.writeString(instance.toString());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public TypeRelativeObjectIdentifier instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static TypeRelativeObjectIdentifier instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return TypeRelativeObjectIdentifier.fromEncodedString(streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, TypeRelativeObjectIdentifier instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, TypeRelativeObjectIdentifier instance) {
        // Done by instantiateInstance
    }

}
