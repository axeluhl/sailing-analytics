package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorJsonSerializer implements JsonSerializer<Competitor> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAILID = "sailID";
    public static final String FIELD_NATIONALITY = "nationality";
    public static final String FIELD_NATIONALITY_ISO2 = "nationalityISO2";
    public static final String FIELD_NATIONALITY_ISO3 = "nationalityISO3";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_BOAT = "boat";
  
    private final JsonSerializer<Team> teamJsonSerializer;
    private final JsonSerializer<Boat> boatJsonSerializer;
    
    public static CompetitorJsonSerializer create() {
    	return new CompetitorJsonSerializer(TeamJsonSerializer.create(), BoatJsonSerializer.create());
    }

    public CompetitorJsonSerializer() {
        this(null, null);
    }

    public CompetitorJsonSerializer(JsonSerializer<Team> teamJsonSerializer, JsonSerializer<Boat> teamBoatSerializer) {
        this.teamJsonSerializer = teamJsonSerializer;
        this.boatJsonSerializer = teamBoatSerializer;
    }
    
    @Override
    public JSONObject serialize(Competitor competitor) {
        JSONObject result = new JSONObject();
        
        result.put(FIELD_ID, competitor.getId().toString());
        result.put(FIELD_NAME, competitor.getName());
        result.put(FIELD_SAILID, competitor.getBoat()==null?"":competitor.getBoat().getSailID());
        final Nationality nationality = competitor.getTeam().getNationality();
        result.put(FIELD_NATIONALITY, nationality == null ? "" : nationality.getThreeLetterIOCAcronym());
        CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
        result.put(FIELD_NATIONALITY_ISO2, countryCode == null ? "" : countryCode.getTwoLetterISOCode());
        result.put(FIELD_NATIONALITY_ISO3, countryCode == null ? "" : countryCode.getThreeLetterISOCode());
        if(teamJsonSerializer != null) {
            result.put(FIELD_TEAM, teamJsonSerializer.serialize(competitor.getTeam()));
        }
        if(boatJsonSerializer != null) {
            result.put(FIELD_BOAT, boatJsonSerializer.serialize(competitor.getBoat()));
        }

        return result;
    }
}
