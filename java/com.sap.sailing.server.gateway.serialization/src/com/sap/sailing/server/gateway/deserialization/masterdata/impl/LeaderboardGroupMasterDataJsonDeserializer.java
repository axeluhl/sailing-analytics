package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.impl.EventMasterData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardGroupMasterDataJsonSerializer;

public class LeaderboardGroupMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardGroupMasterData> {
    
    private final JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer;
    
    private final JsonDeserializer<EventMasterData> eventDeserializer;
    
    



    public LeaderboardGroupMasterDataJsonDeserializer(JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer,
            JsonDeserializer<EventMasterData> eventDeserializer) {
        this.leaderboardDeserializer = leaderboardDeserializer;
        this.eventDeserializer = eventDeserializer;
    }

    @Override
    public LeaderboardGroupMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        Set<LeaderboardMasterData> leaderboards = new HashSet<LeaderboardMasterData>();
        JSONArray leaderboardsJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_LEADERBOARDS);
        for (Object leaderboardObject : leaderboardsJson) {
            JSONObject leaderboardJson = (JSONObject) leaderboardObject;
            leaderboards.add(leaderboardDeserializer.deserialize(leaderboardJson));
        }
        JSONArray eventsJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_EVENTS);
        Set<EventMasterData> events = new HashSet<EventMasterData>();
        for (Object eventObject : eventsJson) {
            JSONObject eventJson = (JSONObject) eventObject;
            events.add(eventDeserializer.deserialize(eventJson));
        }
        String name = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_NAME);
        String description = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DESCRIPTION);
        boolean displayGroupsReverse = (Boolean) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DISPLAY_GROUPS_REVERSE);
        LeaderboardMasterData overallLeaderboardMasterData = leaderboardDeserializer.deserialize((JSONObject) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD));
        return new LeaderboardGroupMasterData(name, description, displayGroupsReverse, overallLeaderboardMasterData, leaderboards, events);
    }

}
