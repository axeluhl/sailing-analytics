package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TeamJsonSerializer implements JsonSerializer<Team> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAILORS = "sailors";
    public static final String FIELD_COACH = "coach";
    public static final String FIELD_NATIONALITY = "nationality";
    public static final String FIELD_IMAGE_URI = "imageUri";
    
    private final JsonSerializer<Person> personJsonSerializer;
    
    public static TeamJsonSerializer create() {
    	return new TeamJsonSerializer(new PersonJsonSerializer(new NationalityJsonSerializer()));
    }

    public TeamJsonSerializer(JsonSerializer<Person> personJsonSerializer) {
        this.personJsonSerializer = personJsonSerializer;
    }
    
    @Override
    public JSONObject serialize(Team team) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, team.getName());
        
        result.put(FIELD_COACH, personJsonSerializer.serialize(team.getCoach()));
        
        JSONArray jsonSailors = new JSONArray();
        for (Person sailor : team.getSailors()) {
            jsonSailors.add(personJsonSerializer.serialize(sailor));
        }
        result.put(FIELD_SAILORS, jsonSailors);
        
        if (team.getImage() != null){
        	result.put(FIELD_IMAGE_URI, team.getImage().toString());
        }
        
        return result;
    }
}

