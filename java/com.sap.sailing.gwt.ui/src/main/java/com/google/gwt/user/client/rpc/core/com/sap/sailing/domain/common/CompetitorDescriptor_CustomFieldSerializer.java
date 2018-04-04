package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common;

import java.util.List;
import java.util.UUID;

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
        streamWriter.writeString(instance.getRaceName());
        streamWriter.writeString(instance.getFleetName());
        streamWriter.writeObject(instance.getCompetitorUUID());
        streamWriter.writeString(instance.getName());
        streamWriter.writeString(instance.getShortName());
        streamWriter.writeString(instance.getTeamName());
        streamWriter.writeObject(instance.getCountryCode());
        streamWriter.writeObject(instance.getTimeOnTimeFactor());
        streamWriter.writeObject(instance.getTimeOnDistanceAllowancePerNauticalMile());
        streamWriter.writeObject(instance.getBoatUUID());
        streamWriter.writeString(instance.getBoatName());
        streamWriter.writeString(instance.getBoatClassName());
        streamWriter.writeString(instance.getSailNumber());
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
        String eventName = streamReader.readString();
        String regattaName = streamReader.readString();
        String raceName = streamReader.readString();
        String fleetName = streamReader.readString();
        UUID competitorUUID = (UUID) streamReader.readObject();
        String fullName = streamReader.readString();
        String shortName = streamReader.readString();
        String teamName = streamReader.readString();
        CountryCode countryCode = (CountryCode) streamReader.readObject();
        Double timeOnTimeFactor = (Double) streamReader.readObject();
        Duration timeOnDistanceAllowancePerNauticalMile = (Duration) streamReader.readObject();
        UUID boatUUID = (UUID) streamReader.readObject();
        String boatName = streamReader.readString();
        String boatClassName = streamReader.readString();
        String sailNumber = streamReader.readString();
              
        return new CompetitorDescriptor(eventName, regattaName,
                raceName, fleetName, competitorUUID,
                fullName, shortName, teamName,
                persons, countryCode, timeOnTimeFactor,
                timeOnDistanceAllowancePerNauticalMile, boatUUID, boatName, boatClassName, sailNumber);
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
