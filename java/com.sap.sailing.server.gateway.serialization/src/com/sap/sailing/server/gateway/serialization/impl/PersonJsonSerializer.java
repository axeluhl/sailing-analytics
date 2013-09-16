package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PersonJsonSerializer implements JsonSerializer<Person> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NATIONALITY = "nationality";
    public static final String FIELD_DATE_OF_BIRTH = "dateOfBirth";
    
    private final JsonSerializer<Nationality> nationalityJsonSerializer;

    public PersonJsonSerializer(JsonSerializer<Nationality> nationalityJsonSerializer) {
        this.nationalityJsonSerializer = nationalityJsonSerializer;
    }

    @Override
    public JSONObject serialize(Person person) {
        if (person == null) {
            return null;
        }
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, person.getName());
        if (person.getDateOfBirth() != null) {
            result.put(FIELD_DATE_OF_BIRTH, person.getDateOfBirth().getTime());
        }
        result.put(FIELD_DESCRIPTION, person.getDescription());
        result.put(FIELD_NATIONALITY, nationalityJsonSerializer.serialize(person.getNationality()));
        
        return result;
    }
}
