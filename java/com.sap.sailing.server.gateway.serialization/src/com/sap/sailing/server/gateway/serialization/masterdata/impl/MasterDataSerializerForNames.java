package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MasterDataSerializerForNames  {
    
    private final Map<String, LeaderboardGroup> allLeaderboardGroups;
    private final Iterable<Event> allEvents;

    public MasterDataSerializerForNames(Map<String, LeaderboardGroup> allLeaderboardGroups, Iterable<Event> allEvents) {
        this.allLeaderboardGroups = allLeaderboardGroups;
        this.allEvents = allEvents;
        
    }

    public JSONArray serialize(Set<String> requestedLeaderboardGroupNames) {
        JSONArray masterData = new JSONArray();

        for (String name : requestedLeaderboardGroupNames) {
            LeaderboardGroup leaderboardGroup = allLeaderboardGroups.get(name);
            if (leaderboardGroup == null) {
                continue;
            }
            JsonSerializer<LeaderboardGroup> serializer = new LeaderboardGroupMasterDataJsonSerializer(allEvents);
            masterData.add(serializer.serialize(leaderboardGroup));
        }
        
        return masterData;
    }

}
