package com.google.gwt.user.client.rpc.core.com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.security.ui.shared.SecurityServiceSharingDTO;

public class SecurityServiceSharingDTO_CustomFieldSerializer extends CustomFieldSerializer<SecurityServiceSharingDTO> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public SecurityServiceSharingDTO instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static SecurityServiceSharingDTO instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final String jSessionIdCookieDomain = streamReader.readString();
        final String baseUrlForCrossDomainStorage = streamReader.readString();
        return new SecurityServiceSharingDTO(jSessionIdCookieDomain, baseUrlForCrossDomainStorage);
    }

    public static void deserialize(SerializationStreamReader streamReader, SecurityServiceSharingDTO instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, SecurityServiceSharingDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, SecurityServiceSharingDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, SecurityServiceSharingDTO instance)
            throws SerializationException {
        streamWriter.writeString(instance.getjSessionIdCookieDomain());
        streamWriter.writeString(instance.getBaseUrlForCrossDomainStorage());
    }
}