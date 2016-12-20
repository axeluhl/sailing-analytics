package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common;

import java.util.List;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;

public class CompetitorDescriptor_CustomFieldSerializer extends CustomFieldSerializer<CompetitorDescriptor> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, CompetitorDescriptor instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, CompetitorDescriptor instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getPersons());
        streamWriter.writeString(instance.getEventName());
        streamWriter.writeString(instance.getRegattaName());
        streamWriter.writeString(instance.getBoatClassName());
        streamWriter.writeString(instance.getRaceName());
        streamWriter.writeString(instance.getFleetName());
        streamWriter.writeString(instance.getSailNumber());
        streamWriter.writeString(instance.getName());
        streamWriter.writeString(instance.getTeamName());
        streamWriter.writeString(instance.getBoatName());
        streamWriter.writeObject(instance.getCountryCode());
        streamWriter.writeObject(instance.getTimeOnTimeFactor());
        streamWriter.writeObject(instance.getTimeOnDistanceAllowancePerNauticalMile());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public CompetitorDescriptor instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static CompetitorDescriptor instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        @SuppressWarnings("unchecked")
        final List<PersonDTO> persons = (List<PersonDTO>) streamReader.readObject();
        return new CompetitorDescriptor(streamReader.readString(), streamReader.readString(),
                streamReader.readString(), streamReader.readString(), streamReader.readString(),
                streamReader.readString(), streamReader.readString(), streamReader.readString(),
                streamReader.readString(), (CountryCode) streamReader.readObject(),
                persons, (Double) streamReader.readObject(), (Duration) streamReader.readObject());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, CompetitorDescriptor instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, CompetitorDescriptor instance) {
        // Done by instantiateInstance
    }

}
