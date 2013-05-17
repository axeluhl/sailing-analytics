package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Person;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PersonJsonSerializer implements JsonSerializer<Person> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";

    @Override
    public JSONObject serialize(Person person) {
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, person.getName());
        result.put(FIELD_DESCRIPTION, person.getDescription());
        
        return result;
    }
}
