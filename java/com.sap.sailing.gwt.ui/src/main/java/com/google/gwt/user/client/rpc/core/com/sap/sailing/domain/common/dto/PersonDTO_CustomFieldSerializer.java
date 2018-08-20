package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.dto.PersonDTO;

public class PersonDTO_CustomFieldSerializer extends CustomFieldSerializer<PersonDTO> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, PersonDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, PersonDTO instance)
            throws SerializationException {
        streamWriter.writeString(instance.getName());
        streamWriter.writeObject(instance.getDateOfBirth());
        streamWriter.writeString(instance.getDescription());
        streamWriter.writeString(instance.getNationalityThreeLetterIOCAcronym());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public PersonDTO instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static PersonDTO instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new PersonDTO(streamReader.readString(), (Date) streamReader.readObject(),
                streamReader.readString(), streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, PersonDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, PersonDTO instance) {
        // Done by instantiateInstance
    }

}
