package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class OwnershipAnnotation_CustomFieldSerializer extends CustomFieldSerializer<OwnershipAnnotation> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, OwnershipAnnotation instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, OwnershipAnnotation instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getAnnotation());
        streamWriter.writeObject(instance.getIdOfAnnotatedObject());
        streamWriter.writeString(instance.getDisplayNameOfAnnotatedObject());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public OwnershipAnnotation instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static OwnershipAnnotation instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new OwnershipAnnotation((Ownership) streamReader.readObject(), (QualifiedObjectIdentifier) streamReader.readObject(), streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, OwnershipAnnotation instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, OwnershipAnnotation instance) {
        // Done by instantiateInstance
    }

}
