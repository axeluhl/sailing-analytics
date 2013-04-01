package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorJsonSerializer extends CompetitorIdJsonSerializer {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_BOAT = "boat";
    
    private JsonSerializer<Team> teamSerializer;
    private JsonSerializer<Boat> boatSerializer;
    
    public CompetitorJsonSerializer(JsonSerializer<Team> teamSerializer, JsonSerializer<Boat> boatSerializer) {
        this.teamSerializer = teamSerializer;
        this.boatSerializer = boatSerializer;
    }

    @Override
    public JSONObject serialize(Competitor object) {
        JSONObject result = super.serialize(object);
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_TEAM, teamSerializer.serialize(object.getTeam()));
        result.put(FIELD_BOAT, boatSerializer.serialize(object.getBoat()));
        return result;
    }

}
