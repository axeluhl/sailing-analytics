package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorMasterDataJsonSerializer implements JsonSerializer<Competitor> {
    
    private final JsonSerializer<BoatClass> boatClassSerializer;
    private final JsonSerializer<Team> teamSerializer;
    
    

    public CompetitorMasterDataJsonSerializer(JsonSerializer<BoatClass> boatClassSerializer,
            JsonSerializer<Team> teamSerializer) {
        this.boatClassSerializer = boatClassSerializer;
        this.teamSerializer = teamSerializer;
    }

    @Override
    public JSONObject serialize(Competitor competitor) {
        //Check if it is null. This can happen for race log events that specify competitors
        //  but where the race is not tracked.
        if (competitor == null) {
            return null;
        }
        JSONObject jsonCompetitor = new JSONObject();
        jsonCompetitor.put("name", competitor.getName());
        jsonCompetitor.put("id", competitor.getId().toString());
        jsonCompetitor.put("boat", createJsonForBoat(competitor.getBoat()));
        jsonCompetitor.put("team", teamSerializer.serialize(competitor.getTeam()));
        return jsonCompetitor;
    }
    
    private JSONObject createJsonForBoat(Boat boat) {
        JSONObject jsonBoat = new JSONObject();
        jsonBoat.put("name", boat.getName());
        jsonBoat.put("sailID", boat.getSailID());
        jsonBoat.put("boatClass", boatClassSerializer.serialize(boat.getBoatClass()));
        return jsonBoat;
    }

}
