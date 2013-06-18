package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardMasterDataJsonSerializer;

public class LeaderboardMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardMasterData> {
    
    private final JsonDeserializer<Competitor> competitorDeserializer;
    
    public LeaderboardMasterDataJsonDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        this.competitorDeserializer = competitorDeserializer;
    }



    @Override
    public LeaderboardMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_NAME);
        Set<Competitor> competitors = new HashSet<Competitor>();
        JSONArray competitorsJsonArray = (JSONArray) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITORS);
        
        for (Object obj : competitorsJsonArray) {
            JSONObject competitorJson = (JSONObject) obj;
            competitors.add(competitorDeserializer.deserialize(competitorJson));
        }
        return null;
    }

}
