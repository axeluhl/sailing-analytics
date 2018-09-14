package com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class AccessControlListAnnotation_CustomFieldSerializer extends CustomFieldSerializer<AccessControlListAnnotation> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, AccessControlListAnnotation instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, AccessControlListAnnotation instance)
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
    public AccessControlListAnnotation instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static AccessControlListAnnotation instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new AccessControlListAnnotation((AccessControlList) streamReader.readObject(), (QualifiedObjectIdentifier) streamReader.readObject(), streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, AccessControlListAnnotation instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, AccessControlListAnnotation instance) {
        // Done by instantiateInstance
    }

}
