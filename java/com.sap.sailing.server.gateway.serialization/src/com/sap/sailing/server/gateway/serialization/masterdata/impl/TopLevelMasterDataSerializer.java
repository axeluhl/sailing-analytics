package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Will take care of serializing all master data connected to a set of leaderboard groups. They
 * are identified in {@link #serialize(Set)}. The serializer must be created with a reference to all
 * leaderboard groups and all events, so that all necessary data can be found and serialized.
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public class TopLevelMasterDataSerializer  {
    
    private final Map<String, LeaderboardGroup> allLeaderboardGroups;
    private final Iterable<Event> allEvents;

    public TopLevelMasterDataSerializer(Map<String, LeaderboardGroup> allLeaderboardGroups, Iterable<Event> allEvents) {
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
