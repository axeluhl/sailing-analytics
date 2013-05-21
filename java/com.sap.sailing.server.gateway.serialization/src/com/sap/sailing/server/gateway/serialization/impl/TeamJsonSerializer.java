package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TeamJsonSerializer implements JsonSerializer<Team> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAILORS = "sailors";
    
    private final JsonSerializer<Person> personJsonSerializer;

    public TeamJsonSerializer(JsonSerializer<Person> personJsonSerializer) {
        this.personJsonSerializer = personJsonSerializer;
    }
    
    @Override
    public JSONObject serialize(Team team) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, team.getName());
        
        JSONArray jsonSailors = new JSONArray();
        for (Person sailor : team.getSailors()) {
            jsonSailors.add(personJsonSerializer.serialize(sailor));
        }
        result.put(FIELD_SAILORS, jsonSailors);
        
        return result;
    }
}
