package com.google.gwt.user.client.rpc.core.com.sap.sailing.expeditionconnector;

import java.util.UUID;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;

public class ExpeditionDeviceConfiguration_CustomFieldSerializer extends CustomFieldSerializer<ExpeditionDeviceConfiguration> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, ExpeditionDeviceConfiguration instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, ExpeditionDeviceConfiguration instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
        streamWriter.writeObject(instance.getDeviceUuid());
        streamWriter.writeObject(instance.getExpeditionBoatId());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public ExpeditionDeviceConfiguration instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static ExpeditionDeviceConfiguration instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new ExpeditionDeviceConfiguration(streamReader.readString(), (UUID) streamReader.readObject(), (Integer) streamReader.readObject());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, ExpeditionDeviceConfiguration instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, ExpeditionDeviceConfiguration instance) {
        // Done by instantiateInstance
    }

}
