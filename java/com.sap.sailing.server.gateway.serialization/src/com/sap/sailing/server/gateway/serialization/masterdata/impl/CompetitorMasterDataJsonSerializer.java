package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.io.Serializable;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorMasterDataJsonSerializer implements JsonSerializer<Competitor> {
    
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_SAIL_ID = "sailID";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_BOAT = "boat";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID_TYPE = "idType";
    public static final String FIELD_DISPLAY_COLOR = "displayColor";
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
        jsonCompetitor.put(FIELD_NAME, competitor.getName());
        jsonCompetitor.put(FIELD_DISPLAY_COLOR, competitor.getDisplayColor());
        // Special treatment for UUIDs. They are represented as String because JSON doesn't have a way to represent them
        // otherwise. However, other, e.g., numeric, types used to encode a serializable ID must be preserved according
        // to JSON semantics.
        // Also see the corresponding case distinction in the deserialized which first tries to parse a string as a UUID
        // becore returning the ID as is.
        jsonCompetitor.put(FIELD_ID_TYPE, competitor.getId().getClass().getName());
        Serializable competitorId = competitor.getId() instanceof UUID ? competitor.getId().toString() : competitor
                .getId();
        jsonCompetitor.put(FIELD_ID, competitorId);
        jsonCompetitor.put(FIELD_BOAT, createJsonForBoat(competitor.getBoat()));
        jsonCompetitor.put(FIELD_TEAM, teamSerializer.serialize(competitor.getTeam()));
        return jsonCompetitor;
    }
    
    private JSONObject createJsonForBoat(Boat boat) {
        JSONObject jsonBoat = new JSONObject();
        jsonBoat.put(FIELD_NAME, boat.getName());
        jsonBoat.put(FIELD_SAIL_ID, boat.getSailID());
        jsonBoat.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(boat.getBoatClass()));
        return jsonBoat;
    }

}
