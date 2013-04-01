package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TeamJsonSerializer implements JsonSerializer<Team> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_NATIONALITY = "nationality";
    
    private JsonSerializer<Nationality> nationalitySerializer;
    
    public TeamJsonSerializer(JsonSerializer<Nationality> nationalitySerializer) {
        this.nationalitySerializer = nationalitySerializer;
    }

    @Override
    public JSONObject serialize(Team object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_NATIONALITY, nationalitySerializer.serialize(object.getNationality()));
        return result;
    }

}
