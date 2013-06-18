package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardGroupMasterDataJsonSerializer;

public class LeaderboardGroupMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardGroupMasterData> {
    
    private final JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer;
    
    public LeaderboardGroupMasterDataJsonDeserializer(JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer) {
        this.leaderboardDeserializer = leaderboardDeserializer;
    }



    @Override
    public LeaderboardGroupMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        Set<LeaderboardMasterData> leaderboards = new HashSet<LeaderboardMasterData>();
        JSONArray leaderboardsJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_LEADERBOARDS);
        for (Object leaderboardObject : leaderboardsJson) {
            JSONObject leaderboardJson = (JSONObject) leaderboardObject;
            leaderboards.add(leaderboardDeserializer.deserialize(leaderboardJson));
        }
        // TODO Auto-generated method stub
        return null;
    }

}
