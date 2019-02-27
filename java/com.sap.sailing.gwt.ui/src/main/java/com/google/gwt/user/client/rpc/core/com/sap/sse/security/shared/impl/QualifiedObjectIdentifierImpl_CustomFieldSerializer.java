package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;

public class QualifiedObjectIdentifierImpl_CustomFieldSerializer extends CustomFieldSerializer<QualifiedObjectIdentifierImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, QualifiedObjectIdentifierImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, QualifiedObjectIdentifierImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getTypeIdentifier());
        streamWriter.writeObject(instance.getTypeRelativeObjectIdentifier());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public QualifiedObjectIdentifierImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static QualifiedObjectIdentifierImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final String typeIdentifier = streamReader.readString();
        final TypeRelativeObjectIdentifier typeRelativeObjectIdentifier = (TypeRelativeObjectIdentifier) streamReader.readObject();
        return new QualifiedObjectIdentifierImpl(typeIdentifier, typeRelativeObjectIdentifier);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, QualifiedObjectIdentifierImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, QualifiedObjectIdentifierImpl instance) {
        // Done by instantiateInstance
    }

}
