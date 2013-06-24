package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.EventMasterData;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaMasterData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardGroupMasterDataJsonSerializer;

public class LeaderboardGroupMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardGroupMasterData> {
    
    private final JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer;
    
    private final JsonDeserializer<EventMasterData> eventDeserializer;
    
    private final JsonDeserializer<RegattaMasterData> regattaDeserializer;

    public LeaderboardGroupMasterDataJsonDeserializer(JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer,
            JsonDeserializer<EventMasterData> eventDeserializer, JsonDeserializer<RegattaMasterData> regattaDeserializer) {
        this.leaderboardDeserializer = leaderboardDeserializer;
        this.eventDeserializer = eventDeserializer;
        this.regattaDeserializer = regattaDeserializer;
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
        JSONArray regattasJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_REGATTAS);
        Set<RegattaMasterData> regattas = new HashSet<RegattaMasterData>();
        for (Object regattaObject : regattasJson) {
            JSONObject regattaJson = (JSONObject) regattaObject;
            regattas.add(regattaDeserializer.deserialize(regattaJson));
        }
        String name = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_NAME);
        String description = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DESCRIPTION);
        boolean displayGroupsReverse = (Boolean) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DISPLAY_GROUPS_REVERSE);
        LeaderboardMasterData overallLeaderboardMasterData = leaderboardDeserializer.deserialize((JSONObject) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD));
        return new LeaderboardGroupMasterData(name, description, displayGroupsReverse, overallLeaderboardMasterData, leaderboards, events, regattas);
    }

}
