package com.google.gwt.user.client.rpc.core.com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.CountryCodeImpl;

public class CountryCodeImpl_CustomFieldSerializer extends CustomFieldSerializer<CountryCodeImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, CountryCodeImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, CountryCodeImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
        streamWriter.writeString(instance.getTwoLetterISOCode());
        streamWriter.writeString(instance.getThreeLetterISOCode());
        streamWriter.writeString(instance.getIANAInternet());
        streamWriter.writeString(instance.getUNVehicle());
        streamWriter.writeString(instance.getThreeLetterIOCCode());
        streamWriter.writeString(instance.getUNISONumeric());
        streamWriter.writeString(instance.getITUCallPrefix());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public CountryCodeImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static CountryCodeImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new CountryCodeImpl(streamReader.readString(), streamReader.readString(),
                streamReader.readString(), streamReader.readString(), streamReader.readString(), streamReader.readString(),
                streamReader.readString(), streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, CountryCodeImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, CountryCodeImpl instance) {
        // Done by instantiateInstance
    }

}
