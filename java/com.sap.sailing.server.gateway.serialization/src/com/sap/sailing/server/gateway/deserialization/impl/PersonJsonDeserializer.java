package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;

public class PersonJsonDeserializer implements JsonDeserializer<DynamicPerson> {

    private final JsonDeserializer<Nationality> nationalityDeserializer;
    
    public PersonJsonDeserializer(JsonDeserializer<Nationality> nationalityDeserializer) {
        this.nationalityDeserializer = nationalityDeserializer;
    }

    @Override
    public DynamicPerson deserialize(JSONObject object) throws JsonDeserializationException {
        if (object == null) {
            // Coach is often null
            return null;
        }
        String name = (String) object.get(PersonJsonSerializer.FIELD_NAME);
        String description = (String) object.get(PersonJsonSerializer.FIELD_DESCRIPTION);
        Nationality nationality = nationalityDeserializer.deserialize((JSONObject) object
                .get(PersonJsonSerializer.FIELD_NATIONALITY));
        Date dateOfBirth = null;
        if (object.containsKey(PersonJsonSerializer.FIELD_DATE_OF_BIRTH)) {
            dateOfBirth = new Date((Long) object.get(PersonJsonSerializer.FIELD_DATE_OF_BIRTH));
        }
        return new PersonImpl(name, nationality, dateOfBirth, description);
    }

}
